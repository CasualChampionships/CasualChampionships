package net.casual.championships.common.util

import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.commands.success
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.component.event.ClickEventCallback
import net.casual.arcade.utils.component.function
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.scoreboard.getOnlinePlayers
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.network.chat.Component

object CasualCommands {
    private val NOT_SPECTATOR = SimpleCommandExceptionType(CasualComponents.NOT_SPECTATING)
    private val NO_TEAM = SimpleCommandExceptionType(CasualComponents.NO_TEAM)

    fun toggleFullbright(
        minigame: Minigame,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        val toggle = if (minigame.effects.hasFullbright(player)) {
            minigame.effects.removeFullbright(player)
            CasualComponents.DISABLED
        } else {
            minigame.effects.addFullbright(player)
            CasualComponents.ENABLED
        }
        return context.source.success(CasualComponents.TOGGLE_FULLBRIGHT.generate(toggle))
    }

    fun toggleTeamGlow(
        minigame: Minigame,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        val toggle = if (minigame.tags.has(player, CasualTags.HAS_TEAM_GLOW)) {
            minigame.tags.remove(player, CasualTags.HAS_TEAM_GLOW)
            CasualComponents.DISABLED
        } else {
            minigame.tags.add(player, CasualTags.HAS_TEAM_GLOW)
            CasualComponents.ENABLED
        }
        val team = player.team
        if (team != null) {
            for (teammate in team.getOnlinePlayers()) {
                minigame.effects.forceUpdate(teammate, player)
            }
        }
        return context.source.success(CasualComponents.TOGGLE_TEAMGLOW.generate(toggle))
    }

    fun broadcastPositionToTeammates(
        minigame: Minigame,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        val team = player.team

        if (team === null) {
            throw NO_TEAM.create()
        }

        val position = player.position()
        val location = Component.literal("[%.0f, %.0f, %.0f]".format(position.x, position.y, position.z)).lime().function { player ->
            player.lookAt(EntityAnchorArgument.Anchor.EYES, position)
            ClickEventCallback.Result.Success
        }
        minigame.chat.broadcastAsPlayerTo(
            player,
            CasualComponents.BROADCAST_POSITION.generate(location),
            team.getOnlinePlayers(),
            minigame.chat.teamChatFormatter
        )
        return Command.SINGLE_SUCCESS
    }

    fun openSpectatingScreen(
        minigame: Minigame,
        context: CommandContext<CommandSourceStack>
    ): Int {
        val player = context.source.playerOrException
        if (!minigame.players.isAdmin(player) && minigame.players.isPlaying(player)) {
            throw NOT_SPECTATOR.create()
        }

        CasualGuiUtils.createTeamSelectionGui(minigame, player).open()
        return Command.SINGLE_SUCCESS
    }
}