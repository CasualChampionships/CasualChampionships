package net.casual.championships.common.util

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.gui.screen.SelectionGuiBuilder
import net.casual.arcade.gui.screen.SelectionGuiStyle
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.ScreenUtils.addSpectatableTeams
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityAnchorArgument

object CommonCommands {
    private val NOT_SPECTATOR = SimpleCommandExceptionType(CommonComponents.NOT_SPECTATING)
    private val NO_TEAM = SimpleCommandExceptionType(CommonComponents.NO_TEAM)

    fun toggleFullbright(
        minigame: Minigame<*>,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        val toggle = if (minigame.effects.hasFullbright(player)) {
            minigame.effects.removeFullbright(player)
            CommonComponents.DISABLED
        } else {
            minigame.effects.addFullbright(player)
            CommonComponents.ENABLED
        }
        return context.source.success(CommonComponents.TOGGLE_FULLBRIGHT.generate(toggle))
    }

    fun toggleTeamGlow(
        minigame: Minigame<*>,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        val toggle = if (minigame.tags.has(player, CommonTags.HAS_TEAM_GLOW)) {
            minigame.tags.remove(player, CommonTags.HAS_TEAM_GLOW)
            CommonComponents.DISABLED
        } else {
            minigame.tags.add(player, CommonTags.HAS_TEAM_GLOW)
            CommonComponents.ENABLED
        }
        val team = player.team
        if (team != null) {
            for (teammate in team.getOnlinePlayers()) {
                minigame.effects.forceUpdate(teammate, player)
                // minigame.effects.forceUpdate(player, teammate)
            }
        }
        return context.source.success(CommonComponents.TOGGLE_TEAMGLOW.generate(toggle))
    }

    fun broadcastPositionToTeammates(
        minigame: Minigame<*>,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        val team = player.team

        if (team === null) {
            throw NO_TEAM.create()
        }

        val position = player.position()
        val location = "[%.0f, %.0f, %.0f]".format(position.x, position.y, position.z).literal().lime().function {
            it.player.lookAt(EntityAnchorArgument.Anchor.EYES, position)
        }
        return minigame.chat.broadcastAsPlayerTo(
            player,
            CommonComponents.BROADCAST_POSITION.generate(location),
            team.getOnlinePlayers(),
            minigame.chat.teamChatFormatter
        ).commandSuccess()
    }

    fun openSpectatingScreen(
        minigame: Minigame<*>,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        if (!minigame.players.isAdmin(player) && minigame.players.isPlaying(player)) {
            throw NOT_SPECTATOR.create()
        }

        val teams = minigame.teams.getOnlineTeams()
        val builder = SelectionGuiBuilder(player, CommonScreens.named(CommonComponents.SPECTATOR_TITLE))
        if (teams.size < 54) {
            builder.style(SelectionGuiStyle.centered(teams.size))
        }
        builder.addSpectatableTeams(teams) { gui, team ->
            SelectionGuiBuilder(gui, CommonScreens.named(team.displayName))
                .style(SelectionGuiStyle.Companion.centered(5, 3))
        }
        return builder.build().open().commandSuccess()
    }
}