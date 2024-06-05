package net.casual.championships.minigame.lobby

import net.casual.arcade.resources.font.heads.PlayerHeadComponents
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.PlayerUtils
import net.casual.championships.common.ui.tab.CasualPlayerListEntries
import net.casual.championships.duel.DuelRequester
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam

class CasualLobbyPlayerListEntries(
    private val minigame: CasualLobbyMinigame
): CasualPlayerListEntries(minigame) {
    override fun getTeams(server: MinecraftServer): Collection<PlayerTeam> {
        return this.minigame.getAllTeams().sortedWith(NAME_ORDER)
    }

    override fun formatPlayerName(server: MinecraftServer, username: String, team: PlayerTeam): MutableComponent {
        val player = PlayerUtils.player(username)
        if (player == null || !this.minigame.isDueling(player)) {
            return super.formatPlayerName(server, username, team)
        }
        return Component.empty()
            .append(DuelRequester.DUEL_PREFIX)
            .append(ComponentUtils.space(2))
            .append(PlayerHeadComponents.getHeadOrDefault(player))
            .append(ComponentUtils.space(2)).append(username.literal().withStyle(team.color).mini())
    }
}