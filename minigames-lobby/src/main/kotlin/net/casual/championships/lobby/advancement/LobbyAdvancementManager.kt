package net.casual.championships.lobby.advancement

import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.events.server.player.PlayerTryAttackEvent
import net.casual.arcade.events.server.player.PlayerVoidDamageEvent
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.stats.Stat.Companion.increment
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.championships.common.event.MinesweeperWonEvent
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.casual.championships.lobby.stats.LobbyStats
import net.minecraft.server.level.ServerPlayer
import java.text.DecimalFormat
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

class LobbyAdvancementManager(
    private val lobby: LobbyMinigame
): MinigameEventListener {
    private var minesweeperRecord = 127 * 1_000

    @Listener
    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        val (player) = event
        player.grantAdvancement(LobbyAdvancements.UH_OH)

        val stat = this.lobby.stats.getOrCreateStat(player, LobbyStats.LEFT_LOBBY)
        stat.increment()
        if (stat.value >= 20) {
            player.grantAdvancement(LobbyAdvancements.YOU_SHALL_NOT_LEAVE)
        }
    }

    @Listener
    private fun onPlayerTick(event: PlayerTickEvent) {
        val player = event.player
        val pb = this.lobby.stats.getOrCreateStat(player, LobbyStats.MINESWEEPER_RECORD)
        val held = this.lobby.stats.getOrCreateStat(player, LobbyStats.MINESWEEPER_RECORD_HELD)

        if (pb.value == this.minesweeperRecord) {
            held.increment()
            if (held.value.Ticks >= 10.Minutes) {
                player.grantAdvancement(LobbyAdvancements.GAMER)
            }
        } else {
            held.modify { 0 }
        }
    }

    @Listener
    private fun onPlayerAttack(event: PlayerTryAttackEvent) {
        val (player, target) = event
        if (target is ServerPlayer && this.lobby.players.isAdmin(target)) {
            val stat = this.lobby.stats.getOrCreateStat(player, LobbyStats.ATTACK_ADMIN)
            stat.increment()
            if (stat.value >= 50) {
                player.grantAdvancement(LobbyAdvancements.ADMIN_ABUSE)
            }
        }
        event.cancel()
    }

    @Listener
    private fun onMinesweeperWon(event: MinesweeperWonEvent) {
        val (player, duration) = event
        if (duration < 40.seconds) {
            player.grantAdvancement(LobbyAdvancements.OFFICIALLY_BORED)
        }

        val millis = duration.inWholeMilliseconds.toInt()
        val stat = this.lobby.stats.getOrCreateStat(player, LobbyStats.MINESWEEPER_RECORD)
        if (millis < stat.value) {
            stat.modify { millis }
        }

        val formatted = FORMAT.format(duration.toDouble(DurationUnit.SECONDS))
        player.sendSystemMessage(CasualComponents.MINESWEEPER_WON.generate(formatted))
        if (millis < this.minesweeperRecord) {
            this.minesweeperRecord = millis
            val message = CasualComponents.MINESWEEPER_RECORD.generate(player.scoreboardName, formatted)
            this.lobby.chat.broadcast(message)
        }
    }

    companion object {
        private val FORMAT = DecimalFormat("#.00")
    }
}