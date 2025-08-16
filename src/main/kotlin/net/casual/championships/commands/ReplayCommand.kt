package net.casual.championships.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.requiresPermission
import net.casual.arcade.replay.command.BasicReplayCommand
import net.casual.championships.common.util.CommonConfig
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

object ReplayCommand: BasicReplayCommand(CommonConfig.resolve("replays").resolve("custom")) {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return super.create(buildContext).requiresPermission(2)
    }
}