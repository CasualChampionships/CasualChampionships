package net.casual.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.utils.ScreenUtils
import net.casual.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object SpectateCommand: Command {
    private val NOT_SPECTATOR = SimpleCommandExceptionType(Texts.SPECTATOR_NOT_SPECTATING)

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal("spectate").executes(this::execute))
        dispatcher.register(Commands.literal("s").executes(this::execute))
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        if (!player.isSpectator) {
            throw NOT_SPECTATOR.create()
        }
        player.openMenu(ScreenUtils.createSpectatorMenu())
        return 1
    }
}