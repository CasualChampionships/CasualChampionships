package net.casual.championships.lobby.minigame.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.fail
import net.casual.arcade.utils.PlayerUtils.grantAdvancement
import net.casual.championships.common.ui.minesweeper.MinesweeperGui
import net.casual.championships.lobby.advancement.LobbyAdvancements
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.casual.championships.lobby.minigame.LobbyPhase
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

class MinesweeperCommand(private val lobby: LobbyMinigame): CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("minesweeper") {
            executes(::openMinesweeper)
        }
    }

    private fun openMinesweeper(context: CommandContext<CommandSourceStack>): Int {
        if (this.lobby.phase < LobbyPhase.Readying) {
            MinesweeperGui(context.source.playerOrException).open()
            return Command.SINGLE_SUCCESS
        }
        context.source.playerOrException.grantAdvancement(LobbyAdvancements.NOT_NOW)
        return context.source.fail(Component.translatable("casual.duel.cannotMinesweeperNow"))
    }
}