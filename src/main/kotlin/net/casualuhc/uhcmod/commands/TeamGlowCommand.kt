package net.casualuhc.uhcmod.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.casualuhc.uhcmod.extensions.PlayerFlag.TeamGlow
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object TeamGlowCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("teamglow").executes {
                this.toggle(it)
            }.then(
                Commands.literal("enabled").executes {
                    this.toggle(it, true)
                }
            ).then(
                Commands.literal("disabled").executes {
                    this.toggle(it, false)
                }
            )
        )
    }

    private fun toggle(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        return this.toggle(context, !player.flags.has(TeamGlow))
    }

    private fun toggle(context: CommandContext<CommandSourceStack>, set: Boolean): Int {
        context.source.playerOrException.flags.set(TeamGlow, set)
        context.source.sendSystemMessage(Texts.UHC_TEAM_GLOW.generate(set))
        return 1
    }
}