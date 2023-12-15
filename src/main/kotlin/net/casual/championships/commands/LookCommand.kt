package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.commands.arguments.coordinates.Vec2Argument
import net.minecraft.commands.arguments.coordinates.Vec3Argument

object LookCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("look").then(
                Commands.literal("towards").then(
                    Commands.argument("position", Vec3Argument.vec3()).executes(this::position)
                )
            ).then(
                Commands.literal("facing").then(
                    Commands.argument("rotation", Vec2Argument.vec2()).executes(this::rotation)
                )
            )
        )
    }

    private fun position(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val position = Vec3Argument.getVec3(context, "position")
        player.lookAt(EntityAnchorArgument.Anchor.EYES, position)
        return 1
    }

    private fun rotation(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val position = Vec2Argument.getVec2(context, "rotation")
        player.connection.teleport(player.x, player.y, player.z, position.x, position.y)
        return 1
    }
}