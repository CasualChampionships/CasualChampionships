package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.gui.screen.SelectionScreenStyle
import net.casual.arcade.minigame.events.lobby.ReadyChecker
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.singleUseFunction
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ScreenUtils
import net.casual.championships.items.MenuItem
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.minigame.duel.DuelRequester
import net.casual.championships.minigame.duel.DuelSettings
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ScoreHolderArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items

object DuelCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("duel").then(
                Commands.literal("with").then(
                    Commands.argument("players", EntityArgument.players()).executes(this::freeForAll)
                )
            )
        )
    }

    private fun freeForAll(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val settings = DuelSettings()
        val menu = SelectionScreenBuilder().apply {
            style = SelectionScreenStyle.centered(3)
            selection(Items.MAP.named("Configure")) {
                it.openMenu(settings.menu(build()))
            }
            selection(MenuItem.YES.named("Confirm")) {
                it.closeContainer()
                val players = EntityArgument.getPlayers(context, "players")
                confirmFreeForAll(context.source.server, player, players, settings)
            }
            selection(MenuItem.NO.named("Cancel")) {
                it.closeContainer()
            }
        }
        player.openMenu(menu.build())
        return 1
    }

    private fun confirmFreeForAll(
        server: MinecraftServer,
        executing: ServerPlayer,
        players: Collection<ServerPlayer>,
        settings: DuelSettings
    ) {
        var started = false

        val all = HashSet(players)
        all.add(executing)
        val requester = DuelRequester(executing, all)
        val unready = requester.arePlayersReady(players.filter { it !== executing }) {
            if (!started) {
                started = true
                this.startFreeForAll(server, executing, players, settings)
            }
        }
        val startAnyways = "Start duel with accepted players".literal().green().singleUseFunction {
            if (!started) {
                started = true
                val ready = HashSet(players)
                ready.removeAll(unready.toSet())
                this.startFreeForAll(server, executing, players, settings)
            }
        }
        requester.broadcastTo(startAnyways, executing)
    }

    private fun startFreeForAll(
        server: MinecraftServer,
        executing: ServerPlayer,
        players: Collection<ServerPlayer>,
        settings: DuelSettings
    ) {
        val minigame = CasualMinigames.event.createDuelMinigame(server, settings)
        minigame.addPlayer(executing)
        for (player in players) {
            minigame.addPlayer(player)
        }
        minigame.start()
    }
}