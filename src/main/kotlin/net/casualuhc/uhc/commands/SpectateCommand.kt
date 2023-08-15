package net.casualuhc.uhc.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casualuhc.uhc.screen.SpectatorScreen
import net.casualuhc.uhc.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object SpectateCommand: Command {
    private val NOT_SPECTATOR = SimpleCommandExceptionType(Texts.SPECTATOR_NOT_SPECTATING)

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val spectate = dispatcher.register(
            Commands.literal("spectate").executes(this::execute)
        )
        dispatcher.register(Commands.literal("s").redirect(spectate))
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        if (!player.isSpectator) {
            throw NOT_SPECTATOR.create()
        }
        player.openMenu(SpectatorScreen.createScreenFactory(0, false))
        return 1
    }
}