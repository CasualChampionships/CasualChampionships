package net.casual.championships.lobby.minigame.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.commands.*
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.minigame.ready.ReadyChecker
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.isMinigameAdminOrHasPermission
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.arcade.utils.component.command
import net.casual.arcade.utils.component.green
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.teleportTo
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.duel.arena.DuelArenasDataModule
import net.casual.championships.duel.gui.DuelConfigurationGui
import net.casual.championships.duel.kit.DuelKitsDataModule
import net.casual.championships.duel.minigame.DuelMinigame
import net.casual.championships.duel.minigame.DuelSettings
import net.casual.championships.duel.utils.DuelRequester
import net.casual.championships.lobby.advancement.LobbyAdvancements
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.casual.championships.lobby.minigame.LobbyPhase
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.PermissionLevel
import java.util.*

class DuelCommand(private val lobby: LobbyMinigame): CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("duel") {
            executes(::startDuel)
            literal("view") {
                argument("player", EntityArgument.player()) {
                    suggests { _ -> lobby.duels.getDuelingPlayerUsernames() }
                    executes(::viewDueler)
                }
            }
            literal("everyone") {
                executes { context -> duelEveryone(context, null) }
                argument("kit", StringArgumentType.string()) {
                    suggests { _ -> lobby.modules.get<DuelKitsDataModule>()?.names() ?: emptyList() }
                    executes(::duelEveryone)
                }
            }
            literal("accept") {
                executes(::acceptDuel)
            }
            literal("reject") {
                executes(::rejectDuel)
            }
        }
    }

    private fun startDuel(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        this.enforceLobbyReadyingPhase(player)

        val (_, _, settings) = this.getDuelArenasKitsAndSettings(context) ?: return 0
        DuelConfigurationGui(player, settings, this.lobby.players::all, this::requestDuelWith).open()
        return Command.SINGLE_SUCCESS
    }

    private fun viewDueler(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException

        val dueler = EntityArgument.getPlayer(context, "player")
        val minigame = dueler.getMinigame()
        if (minigame !is DuelMinigame) {
            return context.source.fail(Component.translatable("casual.duel.playerNotDueling"))
        }

        if (!this.lobby.duels.hasDuel(minigame)) {
            return context.source.fail("This shouldn't happen, please tell sensei!")
        }

        minigame.players.add(player, true, this.lobby.players.isAdmin(player))
        player.teleportTo(dueler.locationWithLevel)
        return context.source.success(Component.translatable("casual.duel.teleportingToDuel"))
    }

    private fun duelEveryone(
        context: CommandContext<CommandSourceStack>,
        kit: String? = StringArgumentType.getString(context, "kit")
    ): Int {
        val player = context.source.playerOrException
        this.enforceLobbyReadyingPhase(player)

        val (_, kits, settings) = this.getDuelArenasKitsAndSettings(context) ?: return 0
        if (kit != null && kits.names().contains(kit)) {
            settings.kit = kit
        }
        this.requestDuelWith(player, this.lobby.players.all, settings)
        return Command.SINGLE_SUCCESS
    }

    private fun acceptDuel(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        if (!this.lobby.duels.acceptLatestDuel(player)) {
            context.source.fail(Component.translatable("casual.duel.noPendingDuel"))
        }
        return Command.SINGLE_SUCCESS
    }

    private fun rejectDuel(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        if (!this.lobby.duels.rejectLatestDuel(player)) {
            context.source.fail(Component.translatable("casual.duel.noPendingDuel"))
        }
        return Command.SINGLE_SUCCESS
    }

    private fun requestDuelWith(
        initiator: ServerPlayer,
        players: Collection<ServerPlayer>,
        settings: DuelSettings
    ) {
        var started = false

        val duelers = HashSet(players)
        duelers.removeIf { !this.lobby.players.has(it) }
        duelers.add(initiator)

        val requesting = duelers.filter { it != initiator }

        val requester = DuelRequester(initiator, duelers, this.lobby.duels.readyCheckSaver)
        if (requesting.isEmpty() && !initiator.isMinigameAdminOrHasPermission(PermissionLevel.OWNERS)) {
            requester.broadcastTo(Component.translatable("casual.duel.notEnoughPlayers").withMiniFont().red(), initiator)
            return
        }

        val checker = ReadyChecker(requester)
        checker.arePlayersReady(requesting).then {
            started = startDuelWith(started, initiator, duelers, setOf(), requester, settings, false)
        }
        val startAnyways = Component.translatable("casual.duel.clickToStart").withMiniFont().green().function { context ->
            val unready = checker.getUnreadyPlayers(context.server)
            started = startDuelWith(started, initiator, duelers, unready, requester, settings, true)
        }
        requester.broadcastTo(startAnyways, initiator)
    }

    private fun startDuelWith(
        started: Boolean,
        initiator: ServerPlayer,
        duelers: HashSet<ServerPlayer>,
        unready: Collection<ServerPlayer>,
        requester: DuelRequester,
        settings: DuelSettings,
        forced: Boolean
    ): Boolean {
        if (started) {
            if (forced) {
                requester.broadcastTo(Component.translatable("casual.duel.alreadyStarted").withMiniFont().red(), initiator)
            }
            return true
        }
        if (!this.lobby.players.has(initiator) || this.lobby.phase >= LobbyPhase.Readying) {
            requester.broadcastTo(Component.translatable("casual.duel.cannotDuelNow").withMiniFont().red(), initiator)
            initiator.grantAdvancement(LobbyAdvancements.NOT_NOW)
            return false
        }

        val ready = HashSet(duelers)
        if (!this.lobby.players.isAdmin(initiator)) {
            ready.removeAll(unready.toSet())
        }
        ready.removeIf { !this.lobby.players.has(it) }

        if (ready.size <= 1 && !initiator.isMinigameAdminOrHasPermission(PermissionLevel.OWNERS)) {
            requester.broadcastTo(Component.translatable("casual.duel.notEnoughPlayers").withMiniFont().red(), initiator)
            return false
        }

        val duel = this.lobby.duels.createDuel(settings)
        this.lobby.players.transferTo(duel, ready, keepSpectating = false)
        duel.chat.broadcastGame(Component.translatable("casual.duel.starting").withMiniFont().green())
        duel.start()

        val players = if (ready.size > 4) {
            ready.take(4).joinToString(" & ") { it.scoreboardName }
        } else {
            ready.joinToString(" & ") { it.scoreboardName }
        }
        val aboutToDuel = Component.translatable("casual.duel.aboutToDuel", players).withMiniFont()
        for (player in this.lobby.players) {
            if (ready.contains(player)) {
                continue
            }
            requester.broadcastTo(aboutToDuel, player)

            val clickToSpectate = Component.empty().append("[")
                .append(Component.translatable("casual.duel.clickToSpectate"))
                .append("]").command("/duel view ${ready.first().scoreboardName}").lime().withMiniFont()
            requester.broadcastTo(clickToSpectate, player)
        }

        return true
    }

    private data class DuelArenasKitsAndSettings(
        val arenas: DuelArenasDataModule,
        val kits: DuelKitsDataModule,
        val settings: DuelSettings
    )

    private fun getDuelArenasKitsAndSettings(context: CommandContext<CommandSourceStack>): DuelArenasKitsAndSettings? {
        val arenas = this.lobby.modules.get<DuelArenasDataModule>()
            ?: return run { context.source.fail("Lobby has no duel arenas available!"); null }
        val kits = this.lobby.modules.get<DuelKitsDataModule>()
            ?: return run { context.source.fail("Lobby has no duel kits available!"); null }
        val settings = DuelSettings(arenas.all(), kits.all())
        return DuelArenasKitsAndSettings(arenas, kits, settings)
    }

    private fun enforceLobbyReadyingPhase(player: ServerPlayer) {
        if (this.lobby.phase >= LobbyPhase.Readying) {
            player.grantAdvancement(LobbyAdvancements.NOT_NOW)
            throw CANNOT_DUEL_NOW.create()
        }
    }

    companion object {
        private val CANNOT_DUEL_NOW = SimpleCommandExceptionType(Component.translatable("casual.duel.cannotDuelNow"))
    }
}