package net.casual.minigame.uhc

import me.senseiwells.replay.player.PlayerRecorders
import net.casual.CasualMod
import net.casual.arcade.minigame.MinigamePhase
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.task.CancellableTask
import net.casual.arcade.utils.BossbarUtils.then
import net.casual.arcade.utils.BossbarUtils.withDuration
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.clearPlayerInventory
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
import net.casual.minigame.uhc.gui.ActiveBossBar
import net.casual.minigame.uhc.task.BorderFinishTask
import net.casual.minigame.uhc.task.GlowingBossBarTask
import net.casual.minigame.uhc.task.GracePeriodBossBarTask
import net.casual.util.RuleUtils
import net.casual.util.Texts
import net.minecraft.sounds.SoundEvents

enum class UHCPhase: MinigamePhase<UHCMinigame> {
    Grace {
        override fun initialise(minigame: UHCMinigame) {
            minigame.resetTrackers()
            RuleUtils.setActiveGamerules(minigame.server)

            PlayerUtils.forEveryPlayer { player ->
                player.sendSystemMessage(Texts.UHC_GRACE_FIRST.gold())
                player.sendSound(SoundEvents.NOTE_BLOCK_PLING.value())
            }

            val task = GracePeriodBossBarTask(minigame)
                .withDuration(10.Minutes)
                // TODO: Make serializable
                .then { minigame.setPhase(BorderMoving) }
            minigame.scheduler.schedulePhasedCancellable(10.Minutes, task).runOnCancel()

            minigame.addBossbar(ActiveBossBar(minigame))
            minigame.createActiveSidebar()
        }

        override fun end(minigame: UHCMinigame) {
            val message = Texts.UHC_GRACE_OVER.red().bold()
            for (player in minigame.getPlayers()) {
                player.sendSystemMessage(message)
                player.sendSound(SoundEvents.ENDER_DRAGON_GROWL)
            }
            minigame.server.isPvpAllowed = true
            minigame.settings.borderStage = UHCBorderStage.FIRST
        }
    },
    BorderMoving {
        override fun start(minigame: UHCMinigame) {

        }

        override fun initialise(minigame: UHCMinigame) {

        }
    },
    BorderFinished {
        override fun start(minigame: UHCMinigame) {
            val task = GlowingBossBarTask(minigame)
                .withDuration(2.Minutes)
                .then(BorderFinishTask(minigame))
            minigame.scheduler.schedulePhasedCancellable(2.Minutes, task).runOnCancel()
        }

        override fun initialise(minigame: UHCMinigame) {

        }

        override fun end(minigame: UHCMinigame) {
            minigame.removeAllBossbars()
            minigame.removeSidebar()
            minigame.server.isPvpAllowed = false
        }
    },
    GameOver {
        override fun start(minigame: UHCMinigame) {
            val team = TeamManager.getAnyAliveTeam(minigame.getPlayers())
            if (team == null) {
                CasualMod.logger.error("Last team was null!")
                return
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
                for (player in minigame.getPlayers()) {
                    player.clearPlayerInventory()
                    PlayerRecorders.get(player)?.stop()
                }
                CasualMinigame.setLobby(minigame.server)
            }

            minigame.grantFinalAdvancements()
        }
    };

    override val id: String = name.lowercase()
}