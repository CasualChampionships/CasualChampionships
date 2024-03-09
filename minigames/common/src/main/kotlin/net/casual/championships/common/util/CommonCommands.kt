package net.casual.championships.common.util

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ScreenUtils
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityAnchorArgument

object CommonCommands {
    private val NOT_SPECTATOR = SimpleCommandExceptionType(CommonComponents.NOT_SPECTATING_MESSAGE)
    private val NO_TEAM = SimpleCommandExceptionType(CommonComponents.NO_TEAM)

    fun toggleFullbright(
        minigame: Minigame<*>,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        val toggle = if (minigame.effects.hasFullbright(player)) {
            minigame.effects.removeFullbright(player)
            CommonComponents.DISABLED_MESSAGE
        } else {
            minigame.effects.addFullbright(player)
            CommonComponents.ENABLED_MESSAGE
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
            CommonComponents.DISABLED_MESSAGE
        } else {
            minigame.tags.add(player, CommonTags.HAS_TEAM_GLOW)
            CommonComponents.ENABLED_MESSAGE
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
            CommonComponents.BROADCAST_POSITION_MESSAGE.generate(location),
            team.getOnlinePlayers(),
            minigame.chat.teamChatFormatter
        ).commandSuccess()
    }

    fun openSpectatingScreen(
        minigame: Minigame<*>,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        if (!minigame.isAdmin(player) && minigame.isPlaying(player)) {
            throw NOT_SPECTATOR.create()
        }

        val menu = ScreenUtils.createSpectatorMenu(
            components = CommonScreens.named(CommonComponents.SPECTATOR_TITLE_MESSAGE),
            teamFilter = { minigame.teams.getOnlineTeams().contains(it) }
        )
        return player.openMenu(menu).commandSuccess()
    }
}