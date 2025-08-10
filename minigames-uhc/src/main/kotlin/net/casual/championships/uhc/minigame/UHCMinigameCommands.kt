package net.casual.championships.uhc.minigame

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.teleportTo
import net.casual.championships.common.util.CommonCommands
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.uhc.compat.UHCVoicePlugin
import net.casual.championships.uhc.border.UHCBoundaryManager
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class UHCMinigameCommands(
    private val uhc: UHCMinigame
): CommandTree {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext) {
        super.register(dispatcher, buildContext)
        dispatcher.register(CommandTree.buildLiteral("s") {
            executes { CommonCommands.openSpectatingScreen(uhc, it) }
            argument("player", EntityArgument.player()) {
                executes(::teleportToPlayer)
            }
        })
        dispatcher.register(CommandTree.buildLiteral("pos") {
            executes { CommonCommands.broadcastPositionToTeammates(uhc, it) }
        })
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("uhc") {
            literal("player") {
                requiresAdminOrPermission()
                argument("player", EntityArgument.player()) {
                    literal("add") {
                        argument("team", TeamArgument.team()) {
                            argument("teleport", BoolArgumentType.bool()) {
                                executes(::addPlayerToTeam)
                            }
                            executes { addPlayerToTeam(it, false) }
                        }
                    }
                    literal("reset-health") {
                        executes(::resetPlayerHealth)
                    }
                }
            }
            literal("voicechat") {
                requiresAdminOrPermission()
                literal("join") {
                    argument("group", StringArgumentType.word()) {
                        suggests { _, builder ->
                            SharedSuggestionProvider.suggest(uhc.voiceGroups.values.map { it.name }, builder)
                        }
                        argument("player", EntityArgument.player()) {
                            executes(::playerJoinVoiceGroup)
                        }
                        executes(::joinVoiceGroup)
                    }
                }
            }
            literal("border") {
                requiresAdminOrPermission()
                literal("start") {
                    executes(::startBoundaries)
                }
            }
            literal("map") {
                requiresAdminOrPermission()
                literal("give") {
                    executes(::giveMapItems)
                }
                literal("clear") {
                    executes(::clearMaps)
                }
            }
            literal("fullbright") {
                executes { CommonCommands.toggleFullbright(uhc, it) }
            }
            literal("teamglow") {
                executes { CommonCommands.toggleTeamGlow(uhc, it) }
            }
            literal("spectate") {
                executes { CommonCommands.openSpectatingScreen(uhc, it) }
                argument("player", EntityArgument.player()) {
                    executes(::teleportToPlayer)
                }
            }
            literal("pos") {
                executes { CommonCommands.broadcastPositionToTeammates(uhc, it) }
            }
        }
    }

    private fun addPlayerToTeam(
        context: CommandContext<CommandSourceStack>,
        teleport: Boolean = BoolArgumentType.getBool(context, "teleport")
    ): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val team = TeamArgument.getTeam(context, "team")

        val server = context.source.server
        server.scoreboard.addPlayerToTeam(target.scoreboardName, team)
        target.sendSystemMessage(CommonComponents.ADDED_TO_TEAM.generate(team.formattedDisplayName))

        this.uhc.players.setPlaying(target)

        if (teleport) {
            for (player in this.uhc.players.playing) {
                if (team.players.contains(player.scoreboardName) && target != player) {
                    target.teleportTo(player.locationWithLevel)
                    break
                }
            }
        }

        val message = Component.literal("${target.scoreboardName} has joined team ")
            .append(team.formattedDisplayName)
            .append(" and has ${if (teleport) "been teleported to a random teammate" else "not been teleported"}")
        return context.source.success(message, true)
    }

    private fun resetPlayerHealth(context: CommandContext<CommandSourceStack>): Int {
        val target = EntityArgument.getPlayer(context, "player")
        this.uhc.resetPlayerHealth(target)
        return context.source.success("Successfully reset ${target.scoreboardName}'s health")
    }

    private fun playerJoinVoiceGroup(context: CommandContext<CommandSourceStack>): Int {
        return joinVoiceGroup(context, EntityArgument.getPlayer(context, "player"))
    }

    private fun joinVoiceGroup(
        context: CommandContext<CommandSourceStack>,
        player: ServerPlayer = context.source.playerOrException
    ): Int {
        val groupName = StringArgumentType.getString(context, "group")
        val matchingGroups = this.uhc.voiceGroups.values.filter { it.name.equals(groupName) }
        if (matchingGroups.isEmpty()) {
            return context.source.fail("Could not find a group named ${groupName}!")
        }

        UHCVoicePlugin.voicechatApi!!.getConnectionOf(player.uuid)?.group = matchingGroups.first()
        return context.source.success("Successfully added ${player.scoreboardName} to $groupName")
    }

    private fun startBoundaries(context: CommandContext<CommandSourceStack>): Int {
        UHCBoundaryManager.start(this.uhc)
        return context.source.success("Successfully started world borders")
    }

    private fun giveMapItems(context: CommandContext<CommandSourceStack>): Int {
        this.uhc.mapRenderer.getMaps().forEach(context.source.playerOrException::addItem)
        return context.source.success("Successfully gave map items")
    }

    private fun clearMaps(context: CommandContext<CommandSourceStack>): Int {
        this.uhc.mapRenderer.clear()
        return context.source.success("Successfully cleared all maps")
    }

    private fun teleportToPlayer(context: CommandContext<CommandSourceStack>): Int {
        val target = EntityArgument.getPlayer(context, "player")
        val player = context.source.playerOrException
        if (!this.uhc.players.isSpectating(player)) {
            return context.source.fail(CommonComponents.NOT_SPECTATING)
        }
        player.teleportTo(target.locationWithLevel)
        return Command.SINGLE_SUCCESS
    }
}