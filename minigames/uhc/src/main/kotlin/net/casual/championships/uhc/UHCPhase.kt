package net.casual.championships.uhc

import net.casual.arcade.gui.elements.ComponentElements
import net.casual.arcade.gui.elements.SidebarElements
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.level.VanillaDimension
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.BossbarUtils.then
import net.casual.arcade.utils.BossbarUtils.withDuration
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.GameRuleUtils.resetToDefault
import net.casual.arcade.utils.GameRuleUtils.set
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.revokeAllAdvancements
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.Sound
import net.casual.championships.common.task.GlowingBossBarTask
import net.casual.championships.common.task.GracePeriodBossBarTask
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonSounds
import net.casual.championships.common.util.CommonUI
import net.casual.championships.common.util.CommonUI.broadcastGame
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameRules
import net.minecraft.world.phys.Vec2

const val INITIALIZING_ID = "initializing"
const val GRACE_ID = "grace"
const val BORDER_MOVING_ID = "border_moving"
const val BORDER_FINISHED_ID = "border_finished"
const val GAME_OVER_ID = "game_over"

enum class UHCPhase(
    override val id: String
): Phase<UHCMinigame> {
    Initializing(INITIALIZING_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.settings.canPvp.set(false)
            minigame.settings.tickFreezeOnPause.set(true)
            minigame.levels.all().forEach { it.dayTime = 0 }
            minigame.resetWorldBorders()

            for (player in minigame.players) {
                player.revokeAllAdvancements()
            }

            val stage = minigame.settings.borderStage

            val level = when (minigame.settings.startingDimension) {
                VanillaDimension.Overworld -> minigame.overworld
                VanillaDimension.Nether -> minigame.nether
                VanillaDimension.End -> minigame.end
            }
            val range = stage.getStartSizeFor(level, minigame.settings.borderSizeMultiplier) * 0.45
            PlayerUtils.spread(
                level,
                Vec2(0.0F, 0.0F),
                500.0,
                range,
                true,
                minigame.players.playing,
                120
            )

            for (player in minigame.players.spectating) {
                if (player.level() != level) {
                    player.teleportTo(level, 0.0, 200.0, 0.0, 0.0F, 0.0F)
                }
            }

            GlobalTickedScheduler.later {
                minigame.setPhase(Grace)
            }
        }

        override fun initialize(minigame: UHCMinigame) {
            // Fantasy does not save game rule data, we must always reset it.
            minigame.setGameRules {
                resetToDefault()
                set(GameRules.RULE_NATURAL_REGENERATION, false)
                set(GameRules.RULE_DOINSOMNIA, false)
            }

            minigame.teams.hideNameTags()

            minigame.ui.removeAllNameTags()
            minigame.ui.addNameTag(CommonUI.createPlayingNameTag())
            minigame.ui.addNameTag(CommonUI.createPlayingHealthTag())

            val sidebar = ArcadeSidebar(ComponentElements.of(UHCComponents.Bitmap.TITLE))
            // TODO: Configure team sizes
            CommonUI.addTeammates(sidebar, 5)
            sidebar.addRow(SidebarElements.empty())
            CommonUI.addBorderDistanceAndRadius(sidebar)
            sidebar.addRow(SidebarElements.empty())
            minigame.ui.setSidebar(sidebar)
        }
    },
    Grace(GRACE_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.settings.isChatGlobal = false
            minigame.settings.mobsWithNoAIAreFlammable = true
            val duration = minigame.settings.gracePeriod
            val task = GracePeriodBossBarTask(minigame)
                .withDuration(duration - 1.Ticks)
                .then(PhaseChangeTask(minigame, BorderMoving))
            minigame.scheduler.schedulePhasedCancellable(duration, task).runIfCancelled()

            val minutes = duration.minutes
            minigame.chat.broadcastGame(CommonComponents.BORDER_INITIAL_GRACE.generate(minutes).gold().mini())
        }

        override fun end(minigame: UHCMinigame) {
            minigame.chat.broadcastGame(
                CommonComponents.BORDER_GRACE_OVER.red().mini(),
                sound = Sound(CommonSounds.GAME_BORDER_MOVING)
            )
            minigame.settings.canPvp.set(true)
        }
    },
    BorderMoving(BORDER_MOVING_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.startWorldBorders()
        }
    },
    BorderFinished(BORDER_FINISHED_ID) {
        override fun start(minigame: UHCMinigame) {
            val task = GlowingBossBarTask(minigame)
                .withDuration(2.Minutes)
                .then(MinigameTask(minigame, UHCMinigame::onBorderFinish))
            minigame.scheduler.schedulePhasedCancellable(2.Minutes, task).runIfCancelled()
        }

        override fun end(minigame: UHCMinigame) {
            minigame.settings.canPvp.set(false)
        }
    },
    GameOver(GAME_OVER_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.stats.freeze()

            minigame.settings.isChatGlobal = true
            minigame.settings.canTakeDamage.set(false)

            val teams = minigame.teams.getPlayingTeams()
            if (teams.size != 1) {
                val message = "Expected to have one team remaining on game over!"
                PlayerUtils.broadcastToOps(message.literal())
                return
            }
            val team = teams.first()

            minigame.uhcAdvancements.grantFinalAdvancements(team.getOnlinePlayers())
            minigame.winners.clear()
            minigame.winners.addAll(team.players)

            for (player in minigame.players) {
                player.sendTitle(CommonComponents.GAME_WON.generate(team.name).withStyle(team.color).mini())
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
        }
    }
}