package net.casual.championships.common.util

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.championships.common.ui.game.TeamSelectorGui
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.core.component.DataComponents

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

        val selections = minigame.teams.getOnlineTeams().map {
            val head = ItemUtils.colouredHeadForFormatting(it.color, CommonItems.FORWARD_FACING_PLAYER_HEAD)
            head.set(DataComponents.CUSTOM_NAME, it.formattedDisplayName.mini())
            TeamSelectorGui.Selection(it, head)
        }
        return TeamSelectorGui(player, selections).open().commandSuccess()
    }
}