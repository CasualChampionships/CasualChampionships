package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.gui.screen.SelectionScreenBuilder
import net.casual.arcade.gui.screen.SelectionScreenStyle
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.ScreenUtils
import net.casual.championships.items.MenuItem
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.minigame.duel.DuelSettings
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ScoreHolderArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.world.item.Items

object DuelCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("duel").then(
                Commands.literal("free-for-all").then(
                    Commands.argument("players", EntityArgument.players()).executes(this::freeForAll)
                )
            )
        )
    }

    private fun freeForAll(context: CommandContext<CommandSourceStack>): Int {
        val settings = DuelSettings()
        val menu = SelectionScreenBuilder().apply {
            style = SelectionScreenStyle.centered(3)
            selection(Items.MAP.named("Configure")) {
                it.openMenu(settings.menu(build()))
            }
            selection(MenuItem.YES.named("Confirm")) {
                it.closeContainer()

                // TODO: ready check

                val players = EntityArgument.getPlayers(context, "players")
                val minigame = CasualMinigames.event.createDuelMinigame(it.server, settings)
                for (player in players) {
                    minigame.addPlayer(player)
                }
                minigame.start()
            }
            selection(MenuItem.NO.named("Cancel")) {
                it.closeContainer()
            }
        }
        context.source.playerOrException.openMenu(menu.build())
        return 1
    }
}