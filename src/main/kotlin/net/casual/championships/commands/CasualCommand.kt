package net.casual.championships.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.success
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.championships.CasualChampionships
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object CasualCommand: CommandTree {
    // FIXME: Redo this command tree to use arcade's DSL
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("casual").requiresAdminOrPermission().then(
            Commands.literal("team").then(
                Commands.literal("create").executes(this::createTeams)
            ).then(
                Commands.literal("reload").executes(this::reloadTeams)
            )
        ).then(
            Commands.literal("config").then(
                Commands.literal("reload").executes(this::reloadConfig)
            )
        ).then(
            Commands.literal("resources").then(
                Commands.literal("reload").executes(this::reloadResources)
            )
        ).then(
            Commands.literal("lobby").executes(this::returnToLobby)
        ).then(
            Commands.literal("floodgates").then(
                Commands.literal("open").executes { this.floodgates(it, true) }
            ).then(
                Commands.literal("close").executes { this.floodgates(it, false) }
            )
        )
    }

    private fun createTeams(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.minigames.createTeams()
        return 1
    }

    private fun reloadTeams(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.minigames.reloadTeams()
        return 1
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.reload(context.source.server)
        return context.source.success("Successfully reloaded config", true)
    }

    private fun reloadResources(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.minigames.reloadPlayerResources()
        return context.source.success("Resending resources...")
    }

    private fun returnToLobby(context: CommandContext<CommandSourceStack>): Int {
        CasualChampionships.minigames.returnToLobby()
        return context.source.success("Returning to lobby...")
    }

    private fun floodgates(context: CommandContext<CommandSourceStack>, open: Boolean): Int {
        CasualChampionships.minigames.floodgates = open
        return context.source.success("Successfully ${if (open) "opened" else "closed"} the floodgates", true)
    }
}