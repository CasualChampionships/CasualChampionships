package net.casualuhc.uhcmod.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casualuhc.arcade.commands.EnumArgument
import net.casualuhc.uhcmod.extensions.PlayerFlag
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.TeamFlag
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.managers.TeamManager
import net.casualuhc.uhcmod.managers.UHCManager
import net.casualuhc.uhcmod.managers.UHCManager.Phase
import net.casualuhc.uhcmod.managers.UHCManager.Phase.*
import net.casualuhc.uhcmod.screen.RuleScreen
import net.casualuhc.uhcmod.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.Component

object UHCCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("uhc").requires {
                it.hasPermission(4)
            }.then(
                Commands.literal("team").then(
                    Commands.literal("reload").executes {
                        this.reloadTeams()
                    }
                ).then(
                    Commands.literal("fakes").then(
                        Commands.literal("spawn").executes {
                            this.spawnFakes()
                        }
                    ).then(
                        Commands.literal("kill").executes {
                            this.killFakes()
                        }
                    )
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
                            Commands.literal("team").then(
                                Commands.argument("team", TeamArgument.team()).then(
                                    Commands.argument("teleport", BoolArgumentType.bool()).executes {
                                        this.setPlayerTeam(it, false)
                                    }
                                ).executes {
                                    this.setPlayerTeam(it, true)
                                }
                            )
                        ).then(
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
                Commands.literal("phase").executes(this::getPhase)
            ).then(
                Commands.literal("setup").executes {
                    this.setPhase(Setup)
                }
            ).then(
                Commands.literal("lobby").then(
                    Commands.literal("reload")
                ).executes {
                    this.setPhase(Lobby)
                }
            ).then(
                Commands.literal("start").then(
                    Commands.literal("force").executes {
                        this.setPhase(Start)
                    }
                ).executes {
                    this.setPhase(Ready)
                }
            ).then(
                Commands.literal("finish").executes {
                    this.setPhase(End)
                }
            ).then(
                Commands.literal("settings").executes(this::openSettingsMenu)
            ).then(
                Commands.literal("config").then(
                    Commands.literal("reload").executes(this::reloadConfig)
                )
            )
        )
    }

    private fun reloadTeams(): Int {
        TeamManager.createTeams()
        return 1
    }

    private fun spawnFakes(): Int {
        TeamManager.spawnFakeUHCPlayers()
        return 1
    }

    private fun killFakes(): Int {
        TeamManager.killAllFakePlayers()
        return 1
    }

    private fun setTeamFlag(context: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(context, "team")
        val flag = EnumArgument.getEnumeration<TeamFlag>(context, "flag")
        val value = BoolArgumentType.getBool(context, "value")
        team.flags.set(flag, value)
        val message = Component.literal("${flag.name} has been set to $value for ").append(team.formattedDisplayName)
        context.source.sendSuccess(message, true)
        return 1
    }

    private fun getTeamFlags(context: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(context, "team")
        val message = team.formattedDisplayName.append(" has the following flags enabled: ${team.flags.get().joinToString()}")
        context.source.sendSuccess(message, false)
        return 1
    }

    private fun setPlayerTeam(context: CommandContext<CommandSourceStack>, bool: Boolean): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val team = TeamArgument.getTeam(context, "team")
        val teleport = bool || BoolArgumentType.getBool(context, "teleport")
        TeamManager.forceAddPlayer(team, player, teleport)

        val message = Component.literal("${player.scoreboardName} has joined team ")
            .append(team.formattedDisplayName)
            .append(" and has ${if (teleport) "been teleported to a random teammate" else "not been teleported"}")
        context.source.sendSuccess(message, true)
        return 1
    }

    private fun setPlayerFlag(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val flag = EnumArgument.getEnumeration<PlayerFlag>(context, "flag")
        val value = BoolArgumentType.getBool(context, "value")
        player.flags.set(flag, value)
        val message = Component.literal("${flag.name} has been set to $value for ").append(player.displayName)
        context.source.sendSuccess(message, true)
        return 1
    }

    private fun getPlayerFlags(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val message = player.displayName.copy().append(" has the following flags enabled: ${player.flags.get().joinToString()}")
        context.source.sendSuccess(message, false)
        return 1
    }

    private fun getPhase(context: CommandContext<CommandSourceStack>): Int {
        context.source.sendSuccess(Component.literal("The current phase is ${UHCManager.phase.name}"), false)
        return 1
    }

    private fun setPhase(phase: Phase): Int {
        UHCManager.setPhase(phase)
        return 1
    }

    private fun openSettingsMenu(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        player.openMenu(RuleScreen.createScreenFactory(0))
        return 1
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        Config.reload()
        return 1
    }
}