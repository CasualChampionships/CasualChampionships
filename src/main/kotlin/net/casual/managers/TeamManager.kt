package net.casual.managers

import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.team.TeamCreatedEvent
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.TeamUtils.addExtension
import net.casual.CasualMod
import net.casual.extensions.TeamFlag.Ignored
import net.casual.extensions.TeamFlag.Ready
import net.casual.extensions.TeamFlagsExtension
import net.casual.extensions.TeamFlagsExtension.Companion.flags
import net.casual.extensions.TeamUHCExtension
import net.casual.util.Config
import net.casual.util.Texts
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.util.*

object TeamManager {
    private var collisions = Team.CollisionRule.ALWAYS

    val configPath = Config.resolve("teams.json")

    fun getSpectatorTeam(): PlayerTeam {
        return Arcade.server.scoreboard.getPlayerTeam("Spectator")!!
    }

    fun Team.hasAlivePlayers(): Boolean {
        val names = this.players
        for (name in names) {
            val player = Arcade.server.playerList.getPlayerByName(name)
            if (player != null && player.isSurvival) {
                return true
            }
        }
        return false
    }

    fun isOneTeamRemaining(): Boolean {
        var team: Team? = null
        for (player in PlayerUtils.players()) {
            if (player.isSurvival) {
                team = if (team === null || team.flags.has(Ignored)) player.team else team
                if (team != null && !team.flags.has(Ignored) && team !== player.team) {
                    return false
                }
            }
        }
        return true
    }

    fun getAnyAliveTeam(): Team? {
        for (player in PlayerUtils.players()) {
            if (player.isSurvival) {
                return player.team
            }
        }
        return null
    }

    fun checkAllTeamsReady() {
        if (getUnreadyTeams().isNotEmpty()) {
            return
        }
        PlayerUtils.forEveryPlayer { player ->
            player.sendSystemMessage(Texts.LOBBY_READY_ALL_READY.copy().gold())
            if (player.hasPermissions(4)) {
                player.sendSystemMessage(Component.literal("[Click here to start]").green().withStyle { s ->
                    s.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/casual uhc start force"))
                })
            }
        }
    }

    fun getUnreadyTeams(): List<PlayerTeam> {
        val teams = LinkedList<PlayerTeam>()
        val scoreboard = Arcade.server.scoreboard
        for (team in scoreboard.playerTeams) {
            var teamHasMember = false
            for (name in team.players) {
                val player = Arcade.server.playerList.getPlayerByName(name)
                if (player != null) {
                    teamHasMember = true
                    break
                }
            }

            if (teamHasMember && !team.flags.has(Ready) && !team.flags.has(Ignored)) {
                teams.add(team)
            }
        }
        return teams
    }

    fun createTeams() {
        val scoreboard = Arcade.server.scoreboard
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
            for (operator in Config.getArrayOrDefault("operators")) {
                scoreboard.addPlayerToTeam(operator.asString, operators)
            }
            operators.collisionRule = Team.CollisionRule.NEVER
            operators.flags.set(Ignored, true)

            val spectators = scoreboard.addPlayerTeam("Spectator")
            spectators.color = ChatFormatting.DARK_GRAY
            spectators.collisionRule = Team.CollisionRule.NEVER
            spectators.flags.set(Ignored, true)

            PlayerUtils.forEveryPlayer { player ->
                if (player.team == null) {
                    scoreboard.addPlayerToTeam(player.scoreboardName, spectators)
                }
            }
        }, Arcade.server).exceptionally {
            CasualMod.logger.error("Failed to download teams from database", it)
            null
        }
    }

    fun setCollisions(shouldCollide: Boolean) {
        collisions = if (shouldCollide) Team.CollisionRule.ALWAYS else Team.CollisionRule.NEVER
        for (team in Arcade.server.scoreboard.playerTeams) {
            team.collisionRule = collisions
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<TeamCreatedEvent> { onTeamCreated(it) }
    }

    private fun onTeamCreated(event: TeamCreatedEvent) {
        event.team.addExtension(TeamFlagsExtension())
        event.team.addExtension(TeamUHCExtension())
    }

    private fun createTeamFromJson(json: JsonObject) {
        if (json.has("_id") && json.has("colour") && json.has("prefix") && json.has("members")) {
            val teamName = json["_id"].asString
            val colour = json["colour"].asString
            val prefix = "[${json["prefix"].asString}] "
            val members = json["members"].asJsonArray
            val scoreboard = Arcade.server.scoreboard
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
