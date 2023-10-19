package net.casual.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.casual.arcade.utils.CommandUtils.success
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.TeamUtils.asPlayerTeam
import net.casual.extensions.TeamFlag.Ignored
import net.casual.extensions.TeamFlag.Ready
import net.casual.extensions.TeamFlagsExtension.Companion.flags
import net.casual.managers.TeamManager
import net.casual.util.Texts
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

// TODO: redo ready logic
object ReadyCommand: Command {
    private val NOT_NOW = SimpleCommandExceptionType(Texts.LOBBY_READY_NOT_NOW)
    private val NO_TEAM = SimpleCommandExceptionType(Texts.LOBBY_READY_NO_TEAM)
    private val ALREADY_READY = SimpleCommandExceptionType(Texts.LOBBY_READY_ALREADY)

    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("ready").then(
                Commands.literal("yes").executes {
                    ready(it, true)
                }
            ).then(
                Commands.literal("no").executes {
                    ready(it, false)
                }
            ).then(
                Commands.literal("reset").requires {
                    it.hasPermission(4)
                }.executes(this::unreadyAll)
            ).then(
                Commands.literal("awaiting").requires {
                    it.hasPermission(4)
                }.executes(this::awaiting)
            )
        )
    }

    private fun ready(context: CommandContext<CommandSourceStack>, ready: Boolean): Int {
        val player = context.source.playerOrException
        val minigame = player.getMinigame()
        val team = player.team
        if (team === null || team.flags.has(Ignored)) {
            throw NO_TEAM.create()
        }
        if (team.flags.has(Ready)) {
            throw ALREADY_READY.create()
        }
        team.flags.set(Ready, ready)
        val generator = (if (ready) Texts.LOBBY_IS_READY else Texts.LOBBY_NOT_READY)
        val message = generator.generate(team.asPlayerTeam().formattedDisplayName)
        PlayerUtils.broadcast(message)
        TeamManager.checkAllTeamsReady()
        return 1
    }

    private fun unreadyAll(context: CommandContext<CommandSourceStack>): Int {
        context.source.server.scoreboard.playerTeams.forEach { team ->
            team.flags.set(Ready, false)
        }
        return 1
    }

    private fun awaiting(context: CommandContext<CommandSourceStack>): Int {
        context.source.success(Component.literal("The following teams are not ready:"), true)
        for (team in TeamManager.getUnreadyTeams()) {
            context.source.success(team.displayName, true)
        }
        return 1
    }
}