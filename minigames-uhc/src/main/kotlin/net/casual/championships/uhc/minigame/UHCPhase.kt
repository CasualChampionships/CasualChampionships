package net.casual.championships.uhc.minigame

import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.impl.BossbarTask.Companion.then
import net.casual.arcade.minigame.task.impl.BossbarTask.Companion.withDuration
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter.Companion.teleport
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.TeamUtils.color
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asLocation
import net.casual.arcade.utils.resetToDefault
import net.casual.arcade.utils.set
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.visuals.predicate.EntityObserverPredicate
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate
import net.casual.arcade.visuals.predicate.PlayerObserverPredicate.Companion.toPlayer
import net.casual.championships.common.task.GracePeriodBossbarTask
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualPredicates
import net.casual.championships.common.util.CasualSounds
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.uhc.border.UHCBoundaryManager
import net.casual.championships.uhc.utils.UHCSpreadTeleporter
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameRules
import net.minecraft.world.phys.Vec3

internal const val INITIALIZING_ID = "initializing"
internal const val GRACE_ID = "grace"
internal const val BOUNDARY_MOVING_ID = "border_moving"
internal const val GAME_OVER_ID = "game_over"

enum class UHCPhase(
    override val id: String
): Phase<UHCMinigame> {
    Initializing(INITIALIZING_ID) {
        override fun start(minigame: UHCMinigame, previous: Phase<UHCMinigame>) {
            minigame.settings.canPvp.set(false)
            minigame.settings.tickFreezeOnPause.set(true)
            minigame.levels.all().forEach { it.dayTime = 0 }
            UHCBoundaryManager.reset(minigame)

            val (level, _) = when (minigame.settings.startingDimension) {
                VanillaDimension.Overworld -> minigame.overworld to null
                VanillaDimension.Nether -> minigame.nether to 120
                VanillaDimension.End -> minigame.end to null
            }
            UHCSpreadTeleporter.teleport(level, minigame.players.playing, true)

            for (player in minigame.players.spectating) {
                if (player.level() != level) {
                    player.teleportTo(level.asLocation(Vec3(0.0, 200.0, 0.0)))
                }
            }

            GlobalTickedScheduler.later {
                minigame.setPhase(Grace)
            }
        }

        override fun initialize(minigame: UHCMinigame) {
            // Fantasy does not save game rule data, we must always reset it.
            minigame.levels.setGameRules {
                resetToDefault()
                set(GameRules.RULE_LOCATOR_BAR, false)
                set(GameRules.RULE_NATURAL_REGENERATION, false)
                set(GameRules.RULE_DOINSOMNIA, false)
                set(GameRules.RULE_DO_IMMEDIATE_RESPAWN, true)
            }

            minigame.teams.hideNameTags()

            minigame.ui.removeAllNametags()
            val observeeNotSpectating = PlayerObserverPredicate { observee, _ ->
                !minigame.players.isSpectating(observee)
            }
            minigame.ui.addNametag(CasualGuiUtils.createPlayingNameTag(
                EntityObserverPredicate.visibleObservee().toPlayer().and(observeeNotSpectating)
            ))
            minigame.ui.addNametag(CasualGuiUtils.createPlayingHealthTag(
                CasualPredicates.VISIBLE_OBSERVER_AND_SPEC_OR_TEAMMATES.and(observeeNotSpectating)
            ))
        }
    },
    Grace(GRACE_ID) {
        override fun start(minigame: UHCMinigame, previous: Phase<UHCMinigame>) {
            minigame.settings.isChatGlobal = false
            minigame.settings.mobsWithNoAIAreFlammable = true
            val duration = minigame.settings.gracePeriod
            val task = GracePeriodBossbarTask(minigame)
                .withDuration(duration - 1.Ticks)
                .then(PhaseChangeTask(minigame, BoundaryMoving))
            minigame.scheduler.schedulePhasedCancellable(duration, task).runIfCancelled()
            // Not starting boundaries, but just to set the time
            minigame.onStartBoundary()

            val minutes = duration.minutes
            minigame.chat.broadcastGame(CasualComponents.BORDER_INITIAL_GRACE.generate(minutes).gold().withMiniFont())
        }

        override fun end(minigame: UHCMinigame, next: Phase<UHCMinigame>) {
            if (this < next) {
                minigame.chat.broadcastGame(
                    CasualComponents.BORDER_GRACE_OVER.red().withMiniFont(),
                    sound = Sound(CasualSounds.GAME_BORDER_MOVING)
                )
                minigame.settings.canPvp.set(true)
            }
        }
    },
    BoundaryMoving(BOUNDARY_MOVING_ID) {
        override fun start(minigame: UHCMinigame, previous: Phase<UHCMinigame>) {
            UHCBoundaryManager.start(minigame)
        }
    },
    GameOver(GAME_OVER_ID) {
        override fun start(minigame: UHCMinigame, previous: Phase<UHCMinigame>) {
            minigame.settings.canPvp.set(false)
            minigame.settings.isChatGlobal = true
            minigame.settings.canTakeDamage.set(false)

            val teams = minigame.teams.getPlayingTeams()
            if (teams.size != 1) {
                val message = "Expected to have one team remaining on game over!"
                minigame.chat.broadcastTo(Component.literal(message), minigame.players.admins)
                return
            }
            val team = teams.first()

            minigame.uhcAdvancements.grantFinalAdvancements(team.getOnlinePlayers())
            minigame.winners.clear()
            minigame.winners.addAll(team.players)

            for (player in minigame.players) {
                player.sendTitle(CasualComponents.GAME_WON.generate(team.name).color(team).withMiniFont())
            }

            // TODO: Better winning screen
            val winTask = MinigameTask(minigame) {
                for (player in it.players) {
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST_FAR, volume = 0.5F)
                }
                it.scheduler.schedulePhased(6.Ticks) {
                    for (player in it.players) {
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_SHOOT, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, volume = 0.5F)
                    }
                }
            }
            minigame.scheduler.schedulePhasedInLoop(0.Ticks, 4.Ticks, 100.Ticks, winTask)

            minigame.scheduler.schedulePhased(20.Seconds, MinigameTask(minigame) {
                minigame.complete()
            })

            minigame.stats.freeze()
        }
    }
}