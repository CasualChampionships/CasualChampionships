package net.casualuhc.uhcmod.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casualuhc.arcade.commands.EnumArgument
import net.casualuhc.arcade.commands.TimeArgument
import net.casualuhc.arcade.commands.TimeZoneArgument
import net.casualuhc.arcade.scheduler.GlobalTickedScheduler
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.utils.CommandSourceUtils.fail
import net.casualuhc.arcade.utils.CommandSourceUtils.success
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.TimeUtils
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.extensions.PlayerFlag
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.TeamFlag
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.managers.PlayerManager
import net.casualuhc.uhcmod.managers.PlayerManager.sendUHCResourcePack
import net.casualuhc.uhcmod.managers.TeamManager
import net.casualuhc.uhcmod.managers.UHCManager
import net.casualuhc.uhcmod.managers.UHCManager.Phase
import net.casualuhc.uhcmod.managers.UHCManager.Phase.*
import net.casualuhc.uhcmod.resources.UHCResourcePack
import net.casualuhc.uhcmod.resources.UHCResourcePackHost
import net.casualuhc.uhcmod.screen.ItemsScreen
import net.casualuhc.uhcmod.screen.RuleScreen
import net.casualuhc.uhcmod.util.Config
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.Component
import java.util.concurrent.TimeUnit

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
                Commands.literal("phase").then(
                    Commands.literal("pause").executes(this::pausePhase)
                ).then(
                    Commands.literal("unpause").executes(this::unpausePhase)
                ).executes(this::getPhase)
            ).then(
                Commands.literal("setup").executes {
                    this.setPhase(Setup)
                }
            ).then(
                Commands.literal("lobby").then(
                    Commands.literal("reload")
                ).then(
                    Commands.literal("delete").executes(this::deleteLobby)
                ).then(
                    Commands.literal("tp").executes(this::teleportToLobby)
                ).executes {
                    this.setPhase(Lobby)
                }
            ).then(
                Commands.literal("start").then(
                    Commands.literal("force").executes {
                        this.setPhase(Start)
                    }
                ).then(
                    Commands.literal("at").then(
                        Commands.argument("time", TimeArgument.time()).then(
                            Commands.argument("zone", TimeZoneArgument.timeZone()).executes(this::atTime)
                        )
                    )
                ).then(
                    Commands.literal("in").then(
                        Commands.argument("time", IntegerArgumentType.integer(1)).then(
                            Commands.argument("unit", EnumArgument.enumeration(TimeUnit::class.java)).executes(this::inTime)
                        )
                    )
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
            ).then(
                Commands.literal("resources").then(
                    Commands.literal("regenerate").executes(this::regenerateResources)
                ).then(
                    Commands.literal("reload").executes(this::reloadResources)
                )
            ).then(
                Commands.literal("items").executes(this::openItemsMenu)
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
        context.source.success(message, true)
        return 1
    }

    private fun getTeamFlags(context: CommandContext<CommandSourceStack>): Int {
        val team = TeamArgument.getTeam(context, "team")
        val message = team.formattedDisplayName.append(" has the following flags enabled: ${team.flags.get().joinToString()}")
        context.source.success(message, false)
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
        context.source.success(message, true)
        return 1
    }

    private fun setPlayerFlag(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val flag = EnumArgument.getEnumeration<PlayerFlag>(context, "flag")
        val value = BoolArgumentType.getBool(context, "value")
        player.flags.set(flag, value)
        val message = Component.literal("${flag.name} has been set to $value for ").append(player.displayName)
        context.source.success(message, true)
        return 1
    }

    private fun getPlayerFlags(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val message = player.displayName.copy().append(" has the following flags enabled: ${player.flags.get().joinToString()}")
        context.source.success(message, false)
        return 1
    }

    private fun pausePhase(context: CommandContext<CommandSourceStack>): Int {
        if (UHCManager.paused) {
            context.source.sendFailure(Component.literal("Current phase was already paused!"))
            return 0
        }
        UHCManager.pause()
        context.source.success(Component.literal("Successfully paused the current phase ${UHCManager.phase}"), true)
        return 1
    }

    private fun unpausePhase(context: CommandContext<CommandSourceStack>): Int {
        if (!UHCManager.paused) {
            context.source.sendFailure(Component.literal("Current phase was not paused!"))
            return 0
        }
        UHCManager.unpause()
        context.source.success(Component.literal("Successfully unpaused the current phase ${UHCManager.phase}"), true)
        return 1
    }

    private fun getPhase(context: CommandContext<CommandSourceStack>): Int {
        val paused = "the scheduler is ${if (UHCManager.paused) "paused" else "running"}"
        context.source.success(Component.literal("The current phase is ${UHCManager.phase.name}, $paused"), false)
        return 1
    }

    private fun setPhase(phase: Phase): Int {
        UHCManager.setPhase(phase)
        return 1
    }

    private fun atTime(context: CommandContext<CommandSourceStack>): Int {
        val time = TimeArgument.getTime(context, "time")
        val zone = TimeZoneArgument.getTimeZone(context, "zone")
        UHCManager.setStartTime(TimeUtils.toEpoch(time, zone) * 1_000)
        context.source.success(Component.literal("Set UHC start time to $time in zone $zone"), true)
        return 1
    }

    private fun inTime(context: CommandContext<CommandSourceStack>): Int {
        val time = IntegerArgumentType.getInteger(context, "time")
        val unit = EnumArgument.getEnumeration<TimeUnit>(context, "unit")
        UHCManager.setStartTime(System.currentTimeMillis() + unit.toMillis(time.toLong()))
        context.source.success(Component.literal("UHC will start in $time ${unit.toChronoUnit()}"), true)
        return 1
    }

    private fun deleteLobby(context: CommandContext<CommandSourceStack>): Int {
        UHCManager.event.getLobbyHandler().getMap().remove()
        context.source.success(Component.literal("Successfully removed the lobby"), false)
        return 1
    }

    private fun teleportToLobby(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        UHCManager.event.getLobbyHandler().forceTeleport(player)
        return 1
    }

    private fun openSettingsMenu(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        player.openMenu(RuleScreen.createScreenFactory(0))
        return 1
    }

    private fun reloadConfig(context: CommandContext<CommandSourceStack>): Int {
        Config.reload()
        context.source.success(Component.literal("Successfully reloaded config"), true)
        return 1
    }

    private fun regenerateResources(context: CommandContext<CommandSourceStack>): Int {
        val success = UHCResourcePack.generate()
        if (success) {
            context.source.success(Component.literal("Successfully regenerated resources, reload resources to refresh clients"))
            return 1
        }
        context.source.fail(Component.literal("Failed to regenerate resources..."))
        return 0
    }

    private fun reloadResources(context: CommandContext<CommandSourceStack>): Int {
        val server = context.source.server
        context.source.success(Component.literal("Reloading resources..."))
        UHCResourcePackHost.reload().thenRunAsync({
            context.source.success(Component.literal("Successfully reloaded resources, resending pack..."))
            PlayerUtils.forEveryPlayer { it.sendUHCResourcePack() }
        }, server)
        return 1
    }

    private fun openItemsMenu(context: CommandContext<CommandSourceStack>): Int {
        context.source.playerOrException.openMenu(ItemsScreen.createScreenFactory(0))
        return 1
    }
}