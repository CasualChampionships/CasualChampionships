package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.championships.managers.TeamManager
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.Config
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object CasualCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>, context: CommandBuildContext) {
        dispatcher.register(
            Commands.literal("casual").requiresAdminOrPermission().then(
                Commands.literal("team").then(
                    Commands.literal("reload").executes {
                        reloadTeams()
                    }
                )
            ).then(
                Commands.literal("config").then(
                    Commands.literal("reload").executes(this::reloadConfig)
                )
            ).then(
                Commands.literal("resources").then(
                    Commands.literal("regenerate").then(
                        Commands.argument("reload", BoolArgumentType.bool()).executes(this::regenerateResources)
                    ).executes { this.regenerateResources(it, false) }
                ).then(
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
        )
    }

    private fun reloadTeams(): Int {
        TeamManager.createTeams()
        return 1
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        Config.reload()
        return context.source.success("Successfully reloaded config", true)
    }

    private fun regenerateResources(
        context: CommandContext<CommandSourceStack>,
        reload: Boolean = BoolArgumentType.getBool(context, "reload")
    ): Int {
        CasualResourcePack.generateAll()
        if (reload) {
            context.source.success("Successfully regenerated resources")
            return this.reloadResources(context)
        }
        return context.source.success("Successfully regenerated resources, reload resources to refresh clients")
    }

    private fun reloadResources(context: CommandContext<CommandSourceStack>): Int {
        CasualResourcePackHost.reload().thenAcceptAsync({
            if (it) {
                context.source.success("Successfully reloaded resources, resending pack...")
                for (player in CasualMinigames.minigame.players) {
                    CasualMinigames.event.sendResourcesTo(player)
                }
            } else {
                context.source.fail("Failed to reload resources...")
            }
        }, context.source.server)
        return context.source.success("Reloading resources...")
    }

    private fun returnToLobby(context: CommandContext<CommandSourceStack>): Int {
        CasualMinigames.event.returnToLobby(context.source.server)
        return context.source.success("Returning to lobby...")
    }

    private fun floodgates(context: CommandContext<CommandSourceStack>, open: Boolean): Int {
        CasualMinigames.floodgates = open
        return context.source.success("Successfully ${if (open) "opened" else "closed"} the floodgates", true)
    }
}