package net.casual.championships.uhc.minigame

import kotlinx.coroutines.launch
import net.casual.arcade.dimensions.level.extensions.LevelClockExtension.Companion.clockExtension
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter.Companion.teleport
import net.casual.arcade.minigame.utils.MinigameUtils.launchPhased
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.coroutine.delay
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.level.resetToDefault
import net.casual.arcade.utils.level.set
import net.casual.arcade.utils.math.location.asLocation
import net.casual.arcade.utils.player.sendSound
import net.casual.arcade.utils.player.sendTitle
import net.casual.arcade.utils.scoreboard.color
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.common.util.CasualSounds
import net.casual.championships.common.util.CasualTags
import net.casual.championships.uhc.border.UHCBoundaryManager
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension.Companion.sharedHealthExtension
import net.casual.championships.uhc.routine.GraceCountdownRoutine
import net.casual.championships.uhc.utils.UHCSpreadTeleporter
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.clock.ClockState
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.phys.Vec3

internal const val INITIALIZING_ID = "initializing"
internal const val GRACE_ID = "grace"
internal const val GAMEPLAY_ID = "gameplay"
internal const val GAME_OVER_ID = "game_over"

enum class UHCPhase(
    override val id: String
): Phase<UHCMinigame> {
    Initializing(INITIALIZING_ID) {
        override fun start(minigame: UHCMinigame, previous: Phase<UHCMinigame>) {
            minigame.levels.setGameRules {
                resetToDefault()
                set(GameRules.IMMEDIATE_RESPAWN, true, minigame.server)
                set(GameRules.LOCATOR_BAR, false, minigame.server)
                set(GameRules.NATURAL_HEALTH_REGENERATION, false)
                set(GameRules.SPAWN_PHANTOMS, false)
            }

            minigame.settings.canPvp.set(false)
            minigame.settings.tickFreezeOnPause.set(true)
            minigame.overworld.clockExtension.set(ClockState(0, 0.0F, 1.0F, false))
            UHCBoundaryManager.reset(minigame)

            val (level, _) = when (minigame.settings.startingDimension) {
                VanillaDimension.Overworld -> minigame.overworld to null
                VanillaDimension.Nether -> minigame.nether to 120
                VanillaDimension.End -> minigame.end to null
            }
            UHCSpreadTeleporter.teleport(level, minigame.players.playing, true)

            for (team in minigame.teams.getPlayingTeams()) {
                val extension = team.sharedHealthExtension
                extension.enabled = minigame.settings.sharingIsCaring
                if (minigame.settings.sharingIsCaring) {
                    extension.set(extension.maxHealth)
                }
            }
            for (player in minigame.players.spectating) {
                if (player.level() != level) {
                    player.teleportTo(level.asLocation(Vec3(0.0, 200.0, 0.0)))
                }
            }

            GlobalTickedScheduler.Server.later {
                minigame.setPhase(Grace)
            }
        }

        override fun initialize(minigame: UHCMinigame) {
            minigame.teams.hideNameTags()

            minigame.visuals.removeAllNametags()
            minigame.visuals.addNametag(CasualGuiUtils.createNametag(minigame))
            minigame.visuals.addNametag(CasualGuiUtils.createPlayingHealthTag(minigame))
        }
    },
    Grace(GRACE_ID) {
        override fun start(minigame: UHCMinigame, previous: Phase<UHCMinigame>) {
            minigame.settings.isChatGlobal = false
            minigame.settings.mobsWithNoAIAreFlammable = true
            minigame.settings.canPvp.set(false)

            minigame.resetBoundaryTimer()
            val borderDelayDuration = minigame.settings.borderStartDelay
            minigame.scheduler.schedule(borderDelayDuration, MinigameTask(minigame) { m ->
                UHCBoundaryManager.start(m)
            })

            val gracePeriodDuration = minigame.settings.gracePeriod
            minigame.scheduler.schedulePhased(0.Ticks, GraceCountdownRoutine(gracePeriodDuration))

            minigame.chat.broadcastGame(
                CasualComponents.BORDER_INITIAL_GRACE.generate(gracePeriodDuration.minutes).gold().withMiniFont()
            )

            val resistanceDuration = gracePeriodDuration.ticks / 2
            for (player in minigame.players.playing) {
                player.addEffect(MobEffectInstance(MobEffects.RESISTANCE, resistanceDuration, 0, false, false, true))
            }
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
    Gameplay(GAMEPLAY_ID),
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

            for (player in team.getOnlinePlayers()) {
                minigame.tags.add(player, CasualTags.WON)
            }

            for (player in minigame.players) {
                player.sendTitle(CasualComponents.GAME_WON.generate(team.name).color(team).withMiniFont())
            }

            // TODO: Better winning screen
            minigame.launchPhased {
                repeat(25) {
                    for (player in minigame.players) {
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST_FAR, volume = 0.5F)
                    }
                    launch {
                        delay(6.Ticks)
                        for (player in minigame.players) {
                            player.sendSound(SoundEvents.FIREWORK_ROCKET_SHOOT, volume = 0.5F)
                            player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                            player.sendSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, volume = 0.5F)
                        }
                    }
                    delay(4.Ticks)
                }
            }

            minigame.scheduler.schedulePhased(20.Seconds, MinigameTask(minigame) {
                minigame.complete()
            })

            minigame.stats.freeze()
        }
    }
}