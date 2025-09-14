package net.casual.championships.sync.data

import net.casual.arcade.utils.TeamUtils.getHexColor
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.PlayerTeam

data class SyncableTeam(
    val name: String,
    val prefix: Component,
    val color: Int?,
    val members: List<String>
) {
    companion object {
        fun from(team: PlayerTeam): SyncableTeam {
            return SyncableTeam(team.name, team.playerPrefix, team.getHexColor(), team.players.toList())
        }
    }
}