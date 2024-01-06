package net.casual.championships.managers

import com.google.gson.JsonObject
import net.casual.championships.CasualMod
import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.team.TeamCreatedEvent
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.TeamUtils.addExtension
import net.casual.championships.extensions.TeamFlag.Ignored
import net.casual.championships.extensions.TeamFlagsExtension
import net.casual.championships.extensions.TeamFlagsExtension.Companion.flags
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.util.Config
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.ServerScoreboard
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.util.*

object TeamManager {
    private const val SPECTATOR = "Spectator"

    private var collisions = Team.CollisionRule.ALWAYS

    val configPath = Config.resolve("teams.json")

    fun Team.hasAlivePlayers(ignore: ServerPlayer? = null): Boolean {
        val names = this.players
        for (name in names) {
            if (ignore != null && ignore.scoreboardName == name) {
                continue
            }
            val player = PlayerUtils.player(name)
            if (player != null && player.isSurvival && !player.isDeadOrDying) {
                return true
            }
        }
        return false
    }

    fun isOneTeamRemaining(players: Collection<ServerPlayer>): Boolean {
        var team: Team? = null
        for (player in players) {
            if (player.isSurvival) {
                team = if (team === null || team.flags.has(Ignored)) player.team else team
                if (team != null && !team.flags.has(Ignored) && team !== player.team) {
                    return false
                }
            }
        }
        return true
    }

    fun getAnyAliveTeam(players: Collection<ServerPlayer>): Team? {
        for (player in players) {
            if (player.isSurvival) {
                return player.team
            }
        }
        return null
    }

    fun createTeams() {
        val scoreboard = Arcade.getServer().scoreboard
        for (team in LinkedList(scoreboard.playerTeams)) {
            scoreboard.removePlayerTeam(team)
        }

        DataManager.database.downloadTeams().thenAcceptAsync({
            for (team in it) {
                createTeamFromJson(team)
            }

            val operators = scoreboard.addPlayerTeam("Operator")
            operators.color = ChatFormatting.WHITE
            operators.playerPrefix = Component.literal("§c[Admin] §r")
            val minigame = CasualMinigames.getCurrent()
            for (operator in Config.operators) {
                val player = PlayerUtils.player(operator)
                if (player != null) {
                    minigame.makeAdmin(player)
                    minigame.makeSpectator(player)
                }
                scoreboard.addPlayerToTeam(operator, operators)
            }
            operators.collisionRule = Team.CollisionRule.NEVER
            operators.flags.set(Ignored, true)

            val spectators = scoreboard.getOrCreateSpectatorTeam()

            PlayerUtils.forEveryPlayer { player ->
                if (player.team == null) {
                    scoreboard.addPlayerToTeam(player.scoreboardName, spectators)
                    minigame.makeSpectator(player)
                }
            }
        }, Arcade.getServer()).exceptionally {
            CasualMod.logger.error("Failed to download teams from database", it)
            null
        }
    }

    fun ServerScoreboard.getOrCreateSpectatorTeam(): PlayerTeam {
        var spectators = this.getPlayerTeam(SPECTATOR)
        if (spectators == null) {
            spectators = this.addPlayerTeam(SPECTATOR)
            spectators.color = ChatFormatting.DARK_GRAY
            spectators.collisionRule = Team.CollisionRule.NEVER
            spectators.nameTagVisibility = Team.Visibility.NEVER
            spectators.flags.set(Ignored, true)
        }
        return spectators!!
    }

    fun setCollisions(shouldCollide: Boolean) {
        collisions = if (shouldCollide) Team.CollisionRule.ALWAYS else Team.CollisionRule.NEVER
        for (team in Arcade.getServer().scoreboard.playerTeams) {
            team.collisionRule = collisions
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<TeamCreatedEvent> { onTeamCreated(it) }
    }

    private fun onTeamCreated(event: TeamCreatedEvent) {
        event.team.addExtension(TeamFlagsExtension())
    }

    private fun createTeamFromJson(json: JsonObject) {
        if (json.has("_id") && json.has("colour") && json.has("prefix") && json.has("members")) {
            val teamName = json["_id"].asString
            val colour = json["colour"].asString
            val prefix = "[${json["prefix"].asString}] "
            val members = json["members"].asJsonArray
            val scoreboard = Arcade.getServer().scoreboard
            var team = scoreboard.getPlayerTeam(teamName)
            if (team == null) {
                team = scoreboard.addPlayerTeam(teamName)!!
            }
            team.playerPrefix = Component.literal(prefix)
            val formatting = ChatFormatting.getByName(colour)
            if (formatting != null) {
                team.color = formatting
            }

            for (member in members) {
                scoreboard.addPlayerToTeam(member.asString, team)
            }
            team.isAllowFriendlyFire = false
            team.collisionRule = collisions
        }
    }
}
