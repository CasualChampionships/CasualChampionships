package net.casual.championships.minigame.lobby_v2

import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.minigame.events.MinigameCloseEvent
import net.casual.arcade.utils.PlayerUtils.username
import net.casual.championships.duel.minigame.DuelMinigame
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

class CasualLobbyDuels(
    private val lobby: CasualLobbyMinigame
) {
    private val duels = ArrayList<DuelMinigame>()

    fun startDuel(duel: DuelMinigame) {
        this.duels.add(duel)
        duel.events.register<MinigameCloseEvent> {
            this.duels.remove(duel)
            duel.players.transferTo(this.lobby)
        }
    }

    fun hasDuel(duel: DuelMinigame): Boolean {
        return this.duels.contains(duel)
    }

    fun isDueling(player: ServerPlayer): Boolean {
        return this.duels.any { duel -> duel.players.has(player) }
    }

    fun getAllTeams(): Set<PlayerTeam> {
        return this.duels.flatMapTo(HashSet()) { duel -> duel.teams.getAllNonSpectatorOrAdminTeams() }
    }

    fun close() {
        for (duel in this.duels.toList()) {
            duel.close()
        }
    }

    fun getDuelingPlayerUsernames(): List<String> {
        return this.duels.flatMap { duel -> duel.players.playing }.map { player -> player.username }
    }
}