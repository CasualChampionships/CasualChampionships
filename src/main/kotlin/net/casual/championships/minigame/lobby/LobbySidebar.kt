package net.casual.championships.minigame.lobby

import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.casual.arcade.utils.component.*
import net.casual.arcade.visuals.elements.ComponentElements
import net.casual.arcade.visuals.elements.PlayerSpecificElement
import net.casual.arcade.visuals.elements.UniversalElement
import net.casual.arcade.visuals.sidebar.DynamicSidebar
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.casual.arcade.visuals.sidebar.SidebarComponents
import net.casual.championships.common.ui.elements.TeammatesSidebarElements
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.minecraft.network.chat.Component

object LobbySidebar {
    fun create(name: String, lobby: LobbyMinigame): DynamicSidebar {
        val title = Component.literal("Casual Championships").withMiniFont().bold().gold()
        val sidebar = DynamicSidebar(ComponentElements.of(title))
        val event = SidebarComponent.withCustomScore(
            Component.literal(" Event:").withMiniFont().red().bold(),
            Component.literal("$name ").withMiniFont().gold()
        )

        val teammates = TeammatesSidebarElements(Component.literal(" "), Component.literal(" "), false)
        val players = UniversalElement.cached { server ->
            val playing = server.scoreboard.playerTeams.filter {
                !lobby.teams.isTeamIgnored(it)
            }
            val online = playing.sumOf { it.getOnlineCount() }
            val expected = playing.sumOf { it.players.size }
            SidebarComponent.withCustomScore(
                Component.literal(" Players: ").withMiniFont().lime().bold(),
                Component.literal("$online/$expected ").withMiniFont().yellow()
            )
        }
        sidebar.setRows(PlayerSpecificElement.composed(players) { player ->
            val components = SidebarComponents.of(SidebarComponent.EMPTY)
            components.addRow(event)
            components.addRow(SidebarComponent.EMPTY)

            val team = player.team
            if (team != null && !lobby.teams.isTeamIgnored(team)) {
                teammates.addTeammates(player, components)
                components.addRow(SidebarComponent.EMPTY)
            }

            components.addRow(players.get(player))
                .addRow(SidebarComponent.EMPTY)
        })
        return sidebar
    }
}