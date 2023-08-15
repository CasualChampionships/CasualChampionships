package net.casualuhc.uhc.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casualuhc.arcade.utils.ComponentUtils.command
import net.casualuhc.arcade.utils.ComponentUtils.lime
import net.casualuhc.arcade.utils.PlayerUtils.teamMessage
import net.casualuhc.uhc.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

object PosCommand: Command {
    private val NO_TEAM = SimpleCommandExceptionType(Texts.COMMAND_NO_TEAM)

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("pos").executes(this::execute)
        )
    }

    private fun execute(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val team = player.team

        if (team === null) {
            throw NO_TEAM.create()
        }

        val pos = player.position()
        val position = Component.literal("[%.0f, %.0f, %.0f]".format(pos.x, pos.y, pos.z))
            .lime()
            .command("/look towards ${pos.x} ${pos.y} ${pos.z}")

        player.teamMessage(Texts.COMMAND_POS.generate(position))
        return 1
    }
}