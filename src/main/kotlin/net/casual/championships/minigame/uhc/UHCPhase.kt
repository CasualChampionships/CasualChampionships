package net.casual.championships.minigame.uhc

import net.casual.arcade.level.VanillaDimension
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casual.arcade.utils.BossbarUtils.then
import net.casual.arcade.utils.BossbarUtils.withDuration
import net.casual.arcade.utils.BossbarUtils.withRemainingDuration
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.GameRuleUtils.resetToDefault
import net.casual.arcade.utils.GameRuleUtils.set
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.revokeAdvancement
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.championships.CasualMod
import net.casual.championships.managers.DataManager
import net.casual.championships.managers.TeamManager
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.minigame.uhc.gui.ActiveBossBar
import net.casual.championships.minigame.uhc.task.GlowingBossBarTask
import net.casual.championships.minigame.uhc.task.GracePeriodBossBarTask
import net.casual.championships.util.Config
import net.casual.championships.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameRules
import net.minecraft.world.phys.Vec2
import net.minecraft.world.scores.Team

const val INITIALIZING_ID = "initializing"
const val GRACE_ID = "grace"
const val BORDER_MOVING_ID = "border_moving"
const val BORDER_FINISHED_ID = "border_finished"
const val GAME_OVER_ID = "game_over"

enum class UHCPhase(
    override val id: String
): MinigamePhase<UHCMinigame> {
    Initializing(INITIALIZING_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.settings.canPvp.set(false)
            minigame.getLevels().forEach { it.dayTime = 0 }
            minigame.resetWorldBorders()
            for (player in minigame.getAllPlayers()) {
                player.sendSystemMessage(Texts.UHC_GRACE_FIRST.gold())
                player.sendSound(SoundEvents.NOTE_BLOCK_PLING.value())

                // TODO: maybe better?
                for (advancement in minigame.server.advancements.allAdvancements) {
                    player.revokeAdvancement(advancement)
                }
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
                minigame.getPlayingPlayers(),
                120
            )

            for (player in minigame.getPlayingPlayers()) {
                minigame.setAsPlaying(player)
            }
            for (player in minigame.getSpectatingPlayers()) {
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

            minigame.ui.setTabDisplay(CasualMinigames.createTabDisplay())

            for (tag in UHCUtils.createNameTags()) {
                minigame.ui.addNameTag(tag)
            }

            minigame.ui.setSidebar(UHCUtils.createSidebar(Config.event.teamSize))
        }
    },
    Grace(GRACE_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.settings.isTeamChat = true
            val duration = minigame.settings.gracePeriod + 19.Ticks
            val task = GracePeriodBossBarTask(minigame)
                .withDuration(duration)
                // .withRemainingDuration(duration + 1.Seconds - minigame.uptime.Ticks)
                .then(PhaseChangeTask(minigame, BorderMoving))
            minigame.scheduler.schedulePhasedCancellable(duration, task).runOnCancel()
        }

        override fun end(minigame: UHCMinigame) {
            val message = Texts.UHC_GRACE_OVER.red().bold()
            for (player in minigame.getAllPlayers()) {
                player.sendSystemMessage(message)
                player.sendSound(SoundEvents.ENDER_DRAGON_GROWL)
            }
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
            minigame.scheduler.schedulePhasedCancellable(2.Minutes, task).runOnCancel()
        }

        override fun end(minigame: UHCMinigame) {
            minigame.ui.removeAllBossbars()
            minigame.ui.removeSidebar()
            minigame.server.isPvpAllowed = false
        }
    },
    GameOver(GAME_OVER_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.stats.freeze()

            minigame.settings.isTeamChat = false
            minigame.settings.canTakeDamage.set(false)

            val teams = minigame.teams.getPlayingTeams()
            if (teams.size != 1) {
                val message = "Expected to have one team remaining on game over!"
                PlayerUtils.broadcastToOps(message.literal())
                return
            }
            val team = teams.first()

            minigame.uhcAdvancements.grantFinalAdvancements(team.getOnlinePlayers())

            for (player in minigame.getAllPlayers()) {
                player.setGlowingTag(false)
                player.sendTitle(Texts.UHC_WON.generate(team.name).withStyle(team.color))
            }

            // TODO:
            DataManager.database.incrementTeamWin(team)

            // TODO: Better winning screen
            val winTask = MinigameTask(minigame) {
                for (player in it.getAllPlayers()) {
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST_FAR, volume = 0.5F)
                }
                it.scheduler.schedulePhased(6.Ticks) {
                    for (player in it.getAllPlayers()) {
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_SHOOT, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, volume = 0.5F)
                    }
                }
            }
            minigame.scheduler.schedulePhasedInLoop(0, 4, 100, Ticks, winTask)

            minigame.scheduler.schedulePhased(20, MinecraftTimeUnit.Seconds, MinigameTask(minigame) {
                CasualMinigames.setLobby(it.server)
            })
        }
    }
}