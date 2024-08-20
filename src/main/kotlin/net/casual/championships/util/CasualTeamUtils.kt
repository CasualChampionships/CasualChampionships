package net.casual.championships.util

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.ServerScoreboard
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

object CasualTeamUtils {
    private const val SPECTATOR = "Spectator"
    private const val ADMIN = "Admin"

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
            admins.nameTagVisibility = Team.Visibility.NEVER
        }
        return admins!!
    }
}