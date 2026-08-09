package net.casual.championships.lobby.minigame.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.async
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.fail
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.commands.suggests
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.get
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.isMinigameAdminOrHasPermission
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.component.command
import net.casual.arcade.utils.component.event.ClickEventCallback
import net.casual.arcade.utils.component.function
import net.casual.arcade.utils.component.green
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.locationWithLevel
import net.casual.arcade.utils.player.grantAdvancement
import net.casual.arcade.utils.player.server
import net.casual.arcade.virtual.visuals.ready.ReadyChecker
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.duel.arena.DuelArenasDataModule
import net.casual.championships.duel.gui.DuelConfigurationGui
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

class DuelCommand(private val lobby: LobbyMinigame): CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("duel") {
            executes(::startDuel)
            literal("view") {
                argument("player", EntityArgument.player()) {
                    suggests { _ -> lobby.duels.getDuelingPlayerUsernames() }
                    executes(::viewDueler)
                }
            }
        }
    }

    private fun startDuel(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        if (this.lobby.phase >= LobbyPhase.Readying) {
            player.grantAdvancement(LobbyAdvancements.NOT_NOW)
            return context.source.fail(Component.translatable("casual.duel.cannotDuelNow"))
        }
        val arenas = this.lobby.modules.get<DuelArenasDataModule>()
            ?: return context.source.fail("Lobby has no duel arenas available!")
        val settings = DuelSettings(arenas.all())
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

    private fun requestDuelWith(
        initiator: ServerPlayer,
        players: Collection<ServerPlayer>,
        settings: DuelSettings
    ) {
        val duelers = HashSet(players)
        duelers.removeIf { !this.lobby.players.has(it) }
        duelers.add(initiator)

        val requesting = duelers.filter { it != initiator }

        val requester = DuelRequester(initiator, duelers)
        if (requesting.isEmpty() && !initiator.isMinigameAdminOrHasPermission(PermissionLevel.OWNERS)) {
            requester.broadcastTo(initiator, Component.translatable("casual.duel.notEnoughPlayers").withMiniFont().red())
            return
        }

        initiator.server.launch {
            var started = false
            val tracker = ReadyChecker.track(requester, requesting)
            val awaiting = async {
                tracker.awaitSuccess()
                started = startDuelWith(started, initiator, duelers, setOf(), requester, settings, false)
            }

            val startAnyways = Component.translatable("casual.duel.clickToStart").withMiniFont().green().function {
                awaiting.cancel()
                val unready = requester.getAccepted()
                started = startDuelWith(started, initiator, duelers, unready, requester, settings, true)
                ClickEventCallback.Result.Success
            }
            requester.broadcastTo(initiator, startAnyways)
        }
    }

    private fun startDuelWith(
        started: Boolean,
        initiator: ServerPlayer,
        duelers: Collection<ServerPlayer>,
        ready: Collection<ServerPlayer>,
        requester: DuelRequester,
        settings: DuelSettings,
        forced: Boolean
    ): Boolean {
        if (started) {
            if (forced) {
                requester.broadcastTo(initiator, Component.translatable("casual.duel.alreadyStarted").withMiniFont().red())
            }
            return true
        }
        if (!this.lobby.players.has(initiator) || this.lobby.phase >= LobbyPhase.Readying) {
            requester.broadcastTo(initiator, Component.translatable("casual.duel.cannotDuelNow").withMiniFont().red())
            initiator.grantAdvancement(LobbyAdvancements.NOT_NOW)
            return false
        }

        val ready = HashSet(if (this.lobby.players.isAdmin(initiator)) duelers else ready)
        ready.removeIf { !this.lobby.players.has(it) }

        if (ready.size <= 1 && !initiator.isMinigameAdminOrHasPermission(PermissionLevel.OWNERS)) {
            requester.broadcastTo(initiator, Component.translatable("casual.duel.notEnoughPlayers").withMiniFont().red())
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
            requester.broadcastTo(player, aboutToDuel)

            val clickToSpectate = Component.empty().append("[")
                .append(Component.translatable("casual.duel.clickToSpectate"))
                .append("]").command("/duel view ${ready.first().scoreboardName}").lime().withMiniFont()
            requester.broadcastTo(player, clickToSpectate)
        }

        return true
    }
}