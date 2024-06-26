package net.casual.championships.managers

import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.utils.PlayerUtils
import net.casual.championships.CasualMod
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.uhc.UHCMod
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.ServerScoreboard
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

object TeamManager {
    private const val SPECTATOR = "Spectator"
    private const val ADMIN = "Admin"

    private var collisions = Team.CollisionRule.ALWAYS

    fun createTeams() {
        val scoreboard = Arcade.getServer().scoreboard
        DataManager.database.downloadTeams().thenAcceptAsync({
            for (team in it) {
                createTeamFromJson(team)
            }

            val minigame = CasualMinigames.minigame
            minigame.teams.setAdminTeam(scoreboard.getOrCreateAdminTeam())
            minigame.teams.setSpectatorTeam(scoreboard.getOrCreateSpectatorTeam())
            for (player in PlayerUtils.players()) {
                if (CasualMinigames.getMinigames().event.isAdmin(player)) {
                    minigame.players.addAdmin(player)
                    val team = player.team
                    if (team == null || team == minigame.teams.getAdminTeam()) {
                        minigame.players.setSpectating(player)
                    }
                }
            }

            for (player in minigame.players) {
                if (player.team == null) {
                    minigame.players.setSpectating(player)
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
        }
        return spectators!!
    }

    fun ServerScoreboard.getOrCreateAdminTeam(): PlayerTeam {
        var admins = this.getPlayerTeam(ADMIN)
        if (admins == null) {
            admins = this.addPlayerTeam(ADMIN)
            admins.color = ChatFormatting.WHITE
            admins.playerPrefix = Component.literal("§c[Admin] §r")
            admins.collisionRule = Team.CollisionRule.NEVER
        }
        return admins!!
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
