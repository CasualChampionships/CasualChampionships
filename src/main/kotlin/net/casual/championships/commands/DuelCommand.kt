package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.gui.screen.SelectionScreenStyle
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.singleUseFunction
import net.casual.arcade.utils.ItemUtils.named
import net.casual.championships.common.item.MenuItem
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.minigame.duel.DuelRequester
import net.casual.championships.minigame.duel.DuelSettings
import net.casual.championships.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items

object DuelCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            // TODO:
            Commands.literal("duel").requires { Config.dev }.then(
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
            selection(MenuItem.GREY_TICK.named("Confirm")) {
                it.closeContainer()
                val players = EntityArgument.getPlayers(context, "players")
                confirmFreeForAll(context.source.server, player, players, settings)
            }
            selection(MenuItem.GREY_CROSS.named("Cancel")) {
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
        val allButExecuting = all.filter { it !== executing }

        val requester = DuelRequester(executing, all)
        if (allButExecuting.isEmpty() && !Config.dev) {
            requester.broadcastTo("Not enough players for a duel!".literal().red(), executing)
            return
        }

        val unready = requester.arePlayersReady(allButExecuting) {
            if (!started) {
                started = true
                this.startFreeForAll(server, executing, players, settings)
            }
        }
        val startAnyways = "Click here to start duel with accepted players".literal().green().singleUseFunction {
            // TODO: Make it so that you cannot start a duel after readying
            if (!started) {
                started = true
                val ready = HashSet(all)
                ready.removeAll(unready.toSet())
                this.startFreeForAll(server, executing, ready, settings)
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