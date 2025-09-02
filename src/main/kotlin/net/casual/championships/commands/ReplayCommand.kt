package net.casual.championships.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.requiresPermission
import net.casual.arcade.replay.command.BasicReplayCommand
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.replay.recorder.settings.SimpleRecorderSettings
import net.casual.championships.common.util.CommonConfig
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path

object ReplayCommand: BasicReplayCommand(CommonConfig.resolve("replays").resolve("custom")) {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return super.create(buildContext).requiresPermission(2)
    }

    override fun createPlayerRecorder(player: ServerPlayer, path: Path, format: ReplayFormat): ReplayPlayerRecorder {
        return ReplayPlayerRecorders.create(player, path, format, SimpleRecorderSettings.DEFAULT.copy(recordVoiceChat = true))
    }
}