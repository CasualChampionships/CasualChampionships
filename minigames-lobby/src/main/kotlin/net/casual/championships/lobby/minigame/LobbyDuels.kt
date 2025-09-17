package net.casual.championships.lobby.minigame

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.minigame.events.MinigameCloseEvent
import net.casual.arcade.utils.PlayerUtils.username
import net.casual.championships.duel.minigame.DuelMinigame
import net.casual.championships.duel.minigame.DuelSettings
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import java.util.*

class LobbyDuels(
    private val lobby: LobbyMinigame
) {
    private val duels = ArrayList<DuelMinigame>()

    fun createDuel(settings: DuelSettings): DuelMinigame {
        val duel = DuelMinigame(this.lobby.server, UUID.randomUUID(), settings, settings.getSelectedArena())
        this.duels.add(duel)
        this.modifyDuel(duel)
        return duel
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

    private fun modifyDuel(duel: DuelMinigame) {
        duel.commands.register(DuelCommand(duel, this.lobby))
        duel.events.register<MinigameCloseEvent> {
            this.duels.remove(duel)
            duel.players.transferTo(this.lobby)
        }
    }

    private class DuelCommand(
        private val duel: DuelMinigame,
        private val lobby: LobbyMinigame
    ): CommandTree {
        override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
            return CommandTree.buildLiteral("duel") {
                literal("leave") {
                    executes(::leaveDuel)
                }
            }
        }

        private fun leaveDuel(context: CommandContext<CommandSourceStack>): Int {
            val player = context.source.playerOrException
            this.duel.players.transferTo(lobby, player, keepSpectating = false)
            return context.source.success("Returning to Lobby...")
        }
    }
}