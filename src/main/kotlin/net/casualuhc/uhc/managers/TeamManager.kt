package net.casualuhc.uhc.managers

import com.google.gson.JsonObject
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.team.TeamCreatedEvent
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.casualuhc.arcade.utils.ComponentUtils.green
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.arcade.utils.TeamUtils.addExtension
import net.casualuhc.uhc.UHCMod
import net.casualuhc.uhc.extensions.TeamFlag.Ignored
import net.casualuhc.uhc.extensions.TeamFlag.Ready
import net.casualuhc.uhc.extensions.TeamFlagsExtension
import net.casualuhc.uhc.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhc.extensions.TeamUHCExtension
import net.casualuhc.uhc.util.Config
import net.casualuhc.uhc.util.Texts
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
        if (this.getUnreadyTeams().isNotEmpty()) {
            return
        }
        PlayerUtils.forEveryPlayer { player ->
            player.sendSystemMessage(Texts.LOBBY_READY_ALL_READY.copy().gold())
            if (player.hasPermissions(4)) {
                player.sendSystemMessage(Component.literal("[Click here to start]").green().withStyle { s ->
                    s.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/uhc start force"))
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
                this.createTeamFromJson(team)
            }

            val operators = scoreboard.addPlayerTeam("Operator")
            operators.color = ChatFormatting.WHITE
            operators.playerPrefix = Component.literal("[OP] ")
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
            UHCMod.logger.error("Failed to download teams from database", it)
            null
        }
    }

    fun setCollisions(shouldCollide: Boolean) {
        this.collisions = if (shouldCollide) Team.CollisionRule.ALWAYS else Team.CollisionRule.NEVER
        for (team in Arcade.server.scoreboard.playerTeams) {
            team.collisionRule = this.collisions
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<TeamCreatedEvent> { this.onTeamCreated(it) }
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
