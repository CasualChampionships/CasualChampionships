package net.casual.championships.uhc.minigame

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.commands.*
import net.casual.arcade.minigame.utils.MinigameUtils.requiresAdminOrPermission
import net.casual.arcade.utils.component.event.ClickEventCallback
import net.casual.arcade.utils.component.function
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.locationWithLevel
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.common.util.CasualTags
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.TeamArgument
import net.minecraft.network.chat.Component

class UHCMinigameCommands(
    private val uhc: UHCMinigame
): CommandTree<CommandSourceStack> {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext) {
        super.register(dispatcher, buildContext)
        dispatcher.register(CommandTree.buildLiteral("s") {
            executes(::openSpectatingScreen)
            argument("player", EntityArgument.player()) {
                executes(::teleportToPlayer)
            }
        })
        dispatcher.register(CommandTree.buildLiteral("pos") {
            executes(::broadcastPositionToTeammates)
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
                executes(::toggleFullbright)
            }
            literal("teamglow") {
                executes(::toggleTeamGlow)
            }
            literal("spectate") {
                executes(::openSpectatingScreen)
                argument("player", EntityArgument.player()) {
                    executes(::teleportToPlayer)
                }
            }
            literal("pos") {
                executes(::broadcastPositionToTeammates)
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
        target.sendSystemMessage(CasualComponents.ADDED_TO_TEAM.generate(team.formattedDisplayName))

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

    private fun startBoundaries(context: CommandContext<CommandSourceStack>): Int {
        this.uhc.boundary.start()
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
            return context.source.fail(CasualComponents.NOT_SPECTATING)
        }
        player.teleportTo(target.locationWithLevel)
        return Command.SINGLE_SUCCESS
    }

    private fun toggleFullbright(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val toggle = if (this.uhc.effects.hasFullbright(player)) {
            this.uhc.effects.removeFullbright(player)
            CasualComponents.DISABLED
        } else {
            this.uhc.effects.addFullbright(player)
            CasualComponents.ENABLED
        }
        return context.source.success(CasualComponents.TOGGLE_FULLBRIGHT.generate(toggle))
    }

    private fun toggleTeamGlow(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val toggle = if (this.uhc.tags.has(player, CasualTags.HAS_TEAM_GLOW)) {
            this.uhc.tags.remove(player, CasualTags.HAS_TEAM_GLOW)
            CasualComponents.DISABLED
        } else {
            this.uhc.tags.add(player, CasualTags.HAS_TEAM_GLOW)
            CasualComponents.ENABLED
        }
        val team = player.team
        if (team != null) {
            for (teammate in team.getOnlinePlayers()) {
                this.uhc.effects.forceUpdate(teammate, player)
            }
        }
        return context.source.success(CasualComponents.TOGGLE_TEAMGLOW.generate(toggle))
    }

    private fun broadcastPositionToTeammates(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        val team = player.team ?: throw NO_TEAM.create()

        val position = player.position()
        val location = Component.literal("[%.0f, %.0f, %.0f]".format(position.x, position.y, position.z)).lime().function { clicker ->
            clicker.lookAt(EntityAnchorArgument.Anchor.EYES, position)
            ClickEventCallback.Result.Success
        }
        this.uhc.chat.broadcastAsPlayerTo(
            player,
            CasualComponents.BROADCAST_POSITION.generate(location),
            team.getOnlinePlayers(),
            this.uhc.chat.teamChatFormatter
        )
        return Command.SINGLE_SUCCESS
    }

    private fun openSpectatingScreen(context: CommandContext<CommandSourceStack>): Int {
        val player = context.source.playerOrException
        if (!this.uhc.players.isAdmin(player) && this.uhc.players.isPlaying(player)) {
            throw NOT_SPECTATOR.create()
        }

        CasualGuiUtils.createTeamSelectionGui(this.uhc, player).open()
        return Command.SINGLE_SUCCESS
    }

    private companion object {
        val NOT_SPECTATOR = SimpleCommandExceptionType(CasualComponents.NOT_SPECTATING)
        val NO_TEAM = SimpleCommandExceptionType(CasualComponents.NO_TEAM)
    }
}
