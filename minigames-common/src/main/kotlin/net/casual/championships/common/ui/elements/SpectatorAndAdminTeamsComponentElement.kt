package net.casual.championships.common.ui.elements

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameTeamManager
import net.casual.arcade.resources.font.heads.PixelGridHeadComponents
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.visuals.elements.UniversalElement
import net.casual.championships.common.util.CasualComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.*

class SpectatorAndAdminTeamsComponentElement(
    private val minigame: Minigame
): UniversalElement<Optional<Component>> {
    override fun get(server: MinecraftServer): Optional<Component> {
        val teams = this.minigame.teams
        val formatted = when {
            teams.hasSpectators() && teams.hasAdmins() -> this.formatSpectatorsAndAdmins(teams)
            teams.hasAdmins() -> this.formatAdmins(teams)
            teams.hasSpectators() -> this.formatSpectators(teams)
            else -> return Optional.empty()
        }
        return Optional.of(formatted)
    }

    private fun formatAdmins(teams: MinigameTeamManager) = Component {
        empty() + CasualComponents.ADMINS.withMiniFont() + nl +
            formatPlayerHeads(teams.getAdminTeam().getOnlinePlayers())
    }

    private fun formatSpectators(teams: MinigameTeamManager) = Component {
        empty() + CasualComponents.SPECTATORS.withMiniFont() + nl +
            formatPlayerHeads(teams.getSpectatorTeam().getOnlinePlayers())
    }

    private fun formatSpectatorsAndAdmins(teams: MinigameTeamManager) = Component {
        val spectators = teams.getSpectatorTeam().getOnlinePlayers()
        val admins = teams.getAdminTeam().getOnlinePlayers()

        val spectatorLength = calculateHeadsLength(spectators.size)
        val adminLength = calculateHeadsLength(admins.size)

        empty() + CasualComponents.SPECTATORS.withMiniFont() + ": " + formatPlayerHeads(spectators) + nl +
            ComponentUtils.widthDifferenceBetween(CasualComponents.SPECTATORS, CasualComponents.ADMINS) +
            CasualComponents.ADMINS.withMiniFont() + ": " + formatPlayerHeads(admins) +
            SpacingFontResources.spaced(spectatorLength - adminLength)
    }

    private fun formatPlayerHeads(players: Iterable<ServerPlayer>): Component {
        val component = Component.empty()
        val iter = players.iterator()
        for (player in iter) {
            component.append(PixelGridHeadComponents.getHeadOrDefaultFor(player))
            if (iter.hasNext()) {
                component.append(SpacingFontResources.spaced(4))
            }
        }
        return component
    }

    private fun MinigameTeamManager.hasSpectators(): Boolean {
        return this.hasSpectatorTeam() && this.getSpectatorTeam().getOnlineCount() > 0
    }

    private fun MinigameTeamManager.hasAdmins(): Boolean {
        return this.hasAdminTeam() && this.getAdminTeam().getOnlineCount() > 0
    }

    private fun calculateHeadsLength(heads: Int): Int {
        return heads * 8 + (heads - 1) * 5
    }
}