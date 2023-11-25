package net.casual.minigame.uhc

import net.casual.CasualMod
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.minigame.task.impl.MinigameTask
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.BossbarUtils.then
import net.casual.arcade.utils.BossbarUtils.withDuration
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.GameRuleUtils.resetToDefault
import net.casual.arcade.utils.GameRuleUtils.set
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.extensions.PlayerFlag
import net.casual.extensions.PlayerFlagsExtension.Companion.flags
import net.casual.managers.DataManager
import net.casual.managers.TeamManager
import net.casual.minigame.CasualMinigame
import net.casual.minigame.uhc.advancement.UHCAdvancements
import net.casual.minigame.uhc.task.GlowingBossBarTask
import net.casual.minigame.uhc.task.GracePeriodBossBarTask
import net.casual.util.Texts
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.GameRules
import net.minecraft.world.scores.Team

const val GRACE_ID = "grace"
const val BORDER_MOVING_ID = "border_moving"
const val BORDER_FINISHED_ID = "border_finished"
const val GAME_OVER_ID = "game_over"

enum class UHCPhase(
    override val id: String
): MinigamePhase<UHCMinigame> {
    Grace(GRACE_ID) {
        override fun start(minigame: UHCMinigame) {
            minigame.setGameRules {
                resetToDefault()
                set(GameRules.RULE_NATURAL_REGENERATION, false)
                set(GameRules.RULE_DOINSOMNIA, false)
            }
            minigame.pvp = false
            minigame.overworld.dayTime = 0
            minigame.resetTrackers()
            minigame.resetWorldBorders()

            for (player in minigame.getPlayers()) {
                player.sendSystemMessage(Texts.UHC_GRACE_FIRST.gold())
                player.sendSound(SoundEvents.NOTE_BLOCK_PLING.value())
            }

            for (team in minigame.getAllPlayerTeams()) {
                team.nameTagVisibility = Team.Visibility.NEVER
            }

            val task = GracePeriodBossBarTask(minigame)
                .withDuration(10.Minutes)
                .then(PhaseChangeTask(minigame, BorderMoving))
            minigame.scheduler.schedulePhasedCancellable(10.Minutes, task).runOnCancel()
        }

        override fun end(minigame: UHCMinigame) {
            val message = Texts.UHC_GRACE_OVER.red().bold()
            for (player in minigame.getPlayers()) {
                player.sendSystemMessage(message)
                player.sendSound(SoundEvents.ENDER_DRAGON_GROWL)
            }
            minigame.pvp = true
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
            val team = TeamManager.getAnyAliveTeam(minigame.getPlayers())
            if (team == null) {
                CasualMod.logger.error("Last team was null!")
                return
            }

            for (players in minigame.getAllPlayerTeams()) {
                players.nameTagVisibility = Team.Visibility.ALWAYS
            }

            val teammates = team.getOnlinePlayers()
            val alive = teammates.filter { it.isSurvival }
            if (alive.size == 1) {
                alive[0].grantAdvancement(UHCAdvancements.LAST_MAN_STANDING)
            }

            for (player in alive) {
                player.abilities.mayfly = true
                player.abilities.invulnerable = true
                player.onUpdateAbilities()
            }

            for (player in teammates) {
                player.flags.set(PlayerFlag.Won, true)
                player.grantAdvancement(UHCAdvancements.WINNER)
            }

            for (player in minigame.getPlayers()) {
                player.setGlowingTag(false)
                player.sendTitle(Texts.UHC_WON.generate(team.name).withStyle(team.color))

                DataManager.database.updateStats(player)
            }

            DataManager.database.incrementTeamWin(team)
            DataManager.database.combineStats()

            minigame.scheduler.schedulePhasedInLoop(0, 4, 100, MinecraftTimeUnit.Ticks) {
                for (player in minigame.getPlayers()) {
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST_FAR, volume = 0.5F)
                }
                minigame.scheduler.schedulePhased(6, MinecraftTimeUnit.Ticks) {
                    for (player in minigame.getPlayers()) {
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_SHOOT, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                        player.sendSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, volume = 0.5F)
                    }
                }
            }

            minigame.scheduler.schedulePhased(20, MinecraftTimeUnit.Seconds) {
                CasualMinigame.setLobby(minigame.server)
            }

            minigame.grantFinalAdvancements()
        }
    }
}