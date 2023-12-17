package net.casual.championships.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.minigame.MinigameResources.Companion.sendTo
import net.casual.arcade.utils.CommandUtils.fail
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.championships.extensions.PlayerFlag
import net.casual.championships.extensions.PlayerFlagsExtension.Companion.flags
import net.casual.championships.extensions.TeamFlag
import net.casual.championships.extensions.TeamFlagsExtension.Companion.flags
import net.casual.championships.managers.TeamManager
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument

object CasualCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("casual").requiresAdminOrPermission().then(
                Commands.literal("team").then(
                    Commands.literal("reload").executes {
                        reloadTeams()
                    }
                ).then(
                    Commands.literal("modify").then(
                        Commands.argument("team", TeamArgument.team()).then(
                            Commands.literal("flags").then(
                                Commands.argument("flag", EnumArgument.enumeration<TeamFlag>()).then(
                                    Commands.argument("value", BoolArgumentType.bool()).executes(this::setTeamFlag)
                                )
                            )
                        )
                    )
                ).then(
                    Commands.literal("get").then(
                        Commands.argument("team", TeamArgument.team()).then(
                            Commands.literal("flags").executes(this::getTeamFlags)
                        )
                    )
                )
            ).then(
                Commands.literal("player").then(
                    Commands.literal("modify").then(
                        Commands.argument("player", EntityArgument.player()).then(
                            Commands.literal("flags").then(
                                Commands.argument("flag", EnumArgument.enumeration<PlayerFlag>()).then(
                                    Commands.argument("value", BoolArgumentType.bool()).executes(this::setPlayerFlag)
                                )
                            )
                        )
                    )
                ).then(
                    Commands.literal("get").then(
                        Commands.argument("player", EntityArgument.player()).then(
                            Commands.literal("flags").executes(this::getPlayerFlags)
                        )
                    )
                )
            ).then(
                Commands.literal("config").then(
                    Commands.literal("reload").executes(this::reloadConfig)
                )
            ).then(
                Commands.literal("resources").then(
                    Commands.literal("regenerate").executes(this::regenerateResources)
                ).then(
                    Commands.literal("reload").executes(this::reloadResources)
                )
            ).then(
                Commands.literal("items").executes(this::openItemsMenu)
            ).then(
                Commands.literal("lobby").executes(this::returnToLobby)
            )
        )
    }

    private fun reloadTeams(): Int {
        TeamManager.createTeams()
        return 1
    }

    private fun setTeamFlag(context: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(context, "team")
        val flag = EnumArgument.getEnumeration<TeamFlag>(context, "flag")
        val value = BoolArgumentType.getBool(context, "value")
        team.flags.set(flag, value)
        val message = "${flag.name} has been set to $value for ".literal().append(team.formattedDisplayName)
        return context.source.success(message, true)
    }

    private fun getTeamFlags(context: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(context, "team")
        val message = team.formattedDisplayName.append(" has the following flags enabled: ${team.flags.get().joinToString()}")
        return context.source.success(message)
    }

    private fun setPlayerFlag(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val flag = EnumArgument.getEnumeration<PlayerFlag>(context, "flag")
        val value = BoolArgumentType.getBool(context, "value")
        player.flags.set(flag, value)
        val message = "${flag.name} has been set to $value for ".literal().append(player.displayName!!)
        return context.source.success(message, true)
    }

    private fun getPlayerFlags(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val message = player.displayName!!.copy().append(" has the following flags enabled: ${player.flags.get().joinToString()}")
        return context.source.success(message)
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        Config.reload()
        return context.source.success("Successfully reloaded config", true)
    }

    private fun regenerateResources(context: CommandContext<CommandSourceStack>): Int {
        if (CasualResourcePack.generate()) {
            return context.source.success("Successfully regenerated resources, reload resources to refresh clients")
        }
        return context.source.fail("Failed to regenerate resources...")
    }

    private fun reloadResources(context: CommandContext<CommandSourceStack>): Int {
        CasualResourcePackHost.reload().thenAcceptAsync({
            if (it) {
                context.source.success("Successfully reloaded resources, resending pack...")
                CasualMinigames.getCurrent().getResources().sendTo(CasualMinigames.getCurrent().getAllPlayers())
            } else {
                context.source.fail("Failed to reload resources...")
            }
        }, context.source.server)
        return context.source.success("Reloading resources...")
    }

    private fun openItemsMenu(context: CommandContext<CommandSourceStack>): Int {
        // context.source.playerOrException.openMenu(ItemsScreen.createScreenFactory(0))
        return 1
    }

    private fun returnToLobby(context: CommandContext<CommandSourceStack>): Int {
        CasualMinigames.setLobby(context.source.server)
        return context.source.success("Returning to lobby...")
    }
}