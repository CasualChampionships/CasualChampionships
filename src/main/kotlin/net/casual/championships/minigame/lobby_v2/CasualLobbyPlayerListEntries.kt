package net.casual.championships.minigame.lobby_v2

import net.casual.arcade.resources.font.heads.PlayerHeadComponents
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.spaced
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.TeamUtils.color
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.literal
import net.casual.arcade.utils.component.plus
import net.casual.arcade.visuals.tab.PlayerListEntries
import net.casual.championships.common.ui.tab.CasualPlayerListEntries
import net.casual.championships.duel.utils.DuelRequester
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

class CasualLobbyPlayerListEntries(
    private val minigame: CasualLobbyMinigame
): CasualPlayerListEntries(minigame) {
    override fun getTeams(server: MinecraftServer): Collection<PlayerTeam> {
        return this.minigame.getAllTeams().sortedWith(NAME_ORDER)
    }

    override fun createPlayerEntry(
        server: MinecraftServer,
        username: String,
        team: PlayerTeam,
        player: ServerPlayer?
    ): PlayerListEntries.Entry {
        if (player == null || !this.minigame.duels.isDueling(player)) {
            return super.createPlayerEntry(server, username, team, player)
        }
        val entry = Component {
            empty() + DuelRequester.DUEL_PREFIX + spaced(2.0F) + PlayerHeadComponents.getHeadOrDefault(player) +
                spaced(2.0F) + literal(username).color(team).withMiniFont()
        }
        return PlayerListEntries.Entry.fromComponent(entry)
    }
}