package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.championships.common.ui.MinesweeperGui
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object MinesweeperCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("minesweeper").executes(this::execute))
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        return MinesweeperGui(context.source.playerOrException).open().commandSuccess()
    }
}