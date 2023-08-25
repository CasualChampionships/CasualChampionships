package net.casual.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.EnumArgument
import net.casual.arcade.commands.TimeArgument
import net.casual.arcade.commands.TimeZoneArgument
import net.casual.arcade.gui.screen.SelectionScreenComponents
import net.casual.arcade.utils.CommandSourceUtils.fail
import net.casual.arcade.utils.CommandSourceUtils.success
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.PlayerUtils.location
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TimeUtils
import net.casual.CasualMod
import net.casual.extensions.PlayerFlag
import net.casual.extensions.PlayerFlagsExtension.Companion.flags
import net.casual.extensions.TeamFlag
import net.casual.extensions.TeamFlagsExtension.Companion.flags
import net.casual.util.UHCPlayerUtils.sendResourcePack
import net.casual.managers.TeamManager
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.resources.UHCResourcePack
import net.casual.resources.CasualResourcePackHost
import net.casual.util.Config
import net.casual.util.Texts
import net.casual.util.UHCPlayerUtils.setForUHC
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.Component
import java.util.concurrent.TimeUnit

object UHCCommand: Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("casual").requires {
                it.hasPermission(4)
            }.then(
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
                            Commands.literal("team").then(
                                Commands.argument("team", TeamArgument.team()).then(
                                    Commands.argument("teleport", BoolArgumentType.bool()).executes {
                                        setPlayerTeam(it, false)
                                    }
                                ).executes {
                                    setPlayerTeam(it, true)
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
            ).then(
                // TODO: cleanup this casting mess
                Commands.literal("uhc").then(
                    Commands.literal("setup").executes {
                        (CasualMod.minigame as UHCMinigame).onSetup().run { 1 }
                    }
                ).then(
                    Commands.literal("lobby").then(
                        Commands.literal("reload")
                    ).then(
                        Commands.literal("delete").executes(this::deleteLobby)
                    ).then(
                        Commands.literal("tp").executes(this::teleportToLobby)
                    ).executes {
                        (CasualMod.minigame as UHCMinigame).onLobby().run { 1 }
                    }
                ).then(
                    Commands.literal("start").then(
                        Commands.literal("force").executes {
                            (CasualMod.minigame as UHCMinigame).onStart().run { 1 }
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
                        (CasualMod.minigame as UHCMinigame).onReady().run { 1 }
                    }
                ).then(
                    Commands.literal("finish").executes {
                        (CasualMod.minigame as UHCMinigame).onEnd().run { 1 }
                    }
                )
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
        val target = EntityArgument.getPlayer(context, "player")
        val team = TeamArgument.getTeam(context, "team")
        val teleport = bool || BoolArgumentType.getBool(context, "teleport")

        val server = context.source.server
        server.scoreboard.addPlayerToTeam(target.scoreboardName, team)
        target.sendSystemMessage(Texts.UHC_ADDED_TO_TEAM.generate(team.formattedDisplayName))

        target.setForUHC(CasualMod.minigame as UHCMinigame, !target.flags.has(PlayerFlag.Participating))

        if (teleport) {
            for (player in PlayerUtils.players()) {
                if (team.players.contains(player.scoreboardName) && player.isSurvival && target != player) {
                    target.teleportTo(player.location)
                    break
                }
            }
        }

        val message = Component.literal("${target.scoreboardName} has joined team ")
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
        if (CasualMod.minigame.paused) {
            context.source.sendFailure(Component.literal("Current phase was already paused!"))
            return 0
        }
        CasualMod.minigame.pause()
        context.source.success(Component.literal("Successfully paused the current phase ${CasualMod.minigame.phase}"), true)
        return 1
    }

    private fun unpausePhase(context: CommandContext<CommandSourceStack>): Int {
        if (!CasualMod.minigame.paused) {
            context.source.sendFailure(Component.literal("Current phase was not paused!"))
            return 0
        }
        CasualMod.minigame.unpause()
        context.source.success(Component.literal("Successfully unpaused the current phase ${CasualMod.minigame.phase}"), true)
        return 1
    }

    private fun getPhase(context: CommandContext<CommandSourceStack>): Int {
        val paused = "the scheduler is ${if (CasualMod.minigame.paused) "paused" else "running"}"
        context.source.success(Component.literal("The current phase is ${CasualMod.minigame.phase.id}, $paused"), false)
        return 1
    }

    private fun atTime(context: CommandContext<CommandSourceStack>): Int {
        val time = TimeArgument.getTime(context, "time")
        val zone = TimeZoneArgument.getTimeZone(context, "zone")
        (CasualMod.minigame as UHCMinigame).setStartTime(TimeUtils.toEpoch(time, zone) * 1_000)
        context.source.success(Component.literal("Set UHC start time to $time in zone $zone"), true)
        return 1
    }

    private fun inTime(context: CommandContext<CommandSourceStack>): Int {
        val time = IntegerArgumentType.getInteger(context, "time")
        val unit = EnumArgument.getEnumeration<TimeUnit>(context, "unit")
        (CasualMod.minigame as UHCMinigame).setStartTime(System.currentTimeMillis() + unit.toMillis(time.toLong()))
        context.source.success(Component.literal("UHC will start in $time ${unit.toChronoUnit()}"), true)
        return 1
    }

    private fun deleteLobby(context: CommandContext<CommandSourceStack>): Int {
        (CasualMod.minigame as UHCMinigame).event.getMinigameLobby().getMap().remove()
        context.source.success(Component.literal("Successfully removed the lobby"), false)
        return 1
    }

    private fun teleportToLobby(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        (CasualMod.minigame as UHCMinigame).event.getMinigameLobby().forceTeleport(player)
        return 1
    }

    private fun openSettingsMenu(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        CasualMod.minigame.openRulesMenu(player, SelectionScreenComponents.DEFAULT)
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
        CasualResourcePackHost.reload().thenRunAsync({
            context.source.success(Component.literal("Successfully reloaded resources, resending pack..."))
            PlayerUtils.forEveryPlayer { it.sendResourcePack(CasualMod.minigame.getResources()) }
        }, server)
        return 1
    }

    private fun openItemsMenu(context: CommandContext<CommandSourceStack>): Int {
        // context.source.playerOrException.openMenu(ItemsScreen.createScreenFactory(0))
        return 1
    }
}