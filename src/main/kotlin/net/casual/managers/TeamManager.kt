package net.casual.managers

import com.google.gson.JsonObject
import net.casual.CasualMod
import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.team.TeamCreatedEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.TeamUtils
import net.casual.arcade.utils.TeamUtils.addExtension
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
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team
import java.util.*

object TeamManager {
    private var collisions = Team.CollisionRule.ALWAYS

    val configPath = Config.resolve("teams.json")

    fun getSpectatorTeam(): PlayerTeam {
        return Arcade.getServer().scoreboard.getPlayerTeam("Spectator")!!
    }

    fun Team.hasAlivePlayers(): Boolean {
        val names = this.players
        for (name in names) {
            val player = PlayerUtils.player(name)
            if (player != null && player.isSurvival) {
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

    fun announceReady(minigame: Minigame<*>) {
        for (team in minigame.getPlayerTeams()) {
            team.flags.set(Ready, false)
        }

        val bar = Component.literal("══════════════════").gold()
        val yes = Component.literal("[").append(Texts.LOBBY_YES).append("]").bold().green().withStyle {
            it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready yes"))
        }
        val no = Component.literal("[").append(Texts.LOBBY_NO).append("]").bold().red().withStyle {
            it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready no"))
        }
        val readyMessage = bar.copy()
            .append("\n      ")
            .append(Texts.LOBBY_READY_QUESTION)
            .append("\n\n\n       ")
            .append(yes)
            .append("        ")
            .append(no)
            .append("\n\n\n")
            .append(bar)

        for (player in minigame.getPlayers()) {
            val team = player.team
            if (team != null && !team.flags.has(Ignored)) {
                player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.MASTER, 1.0F, 1.0F)
                player.sendSystemMessage(readyMessage)
            }
        }
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
        val scoreboard = Arcade.getServer().scoreboard
        for (team in scoreboard.playerTeams) {
            var teamHasMember = false
            for (name in team.players) {
                val player = PlayerUtils.player(name)
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
        }, Arcade.getServer()).exceptionally {
            CasualMod.logger.error("Failed to download teams from database", it)
            null
        }
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
        event.team.addExtension(TeamUHCExtension())
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
