package net.casual.championships.common.ui.elements

import net.casual.arcade.resources.font.heads.PixelGridHeadComponents
import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.italicize
import net.casual.arcade.utils.component.wrap
import net.casual.arcade.utils.player.DynamicResolvableProfile
import net.casual.arcade.utils.player.isSurvival
import net.casual.arcade.utils.player.server
import net.casual.arcade.utils.scoreboard.color
import net.casual.arcade.utils.server.player
import net.casual.arcade.visuals.sidebar.SidebarComponent
import net.casual.arcade.visuals.sidebar.SidebarComponents
import net.casual.championships.common.util.CasualComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

class TeammatesSidebarElements(
    private val buffer: Component,
    private val playerBuffer: Component,
    private val health: Boolean
) {
    fun addTeammates(
        player: ServerPlayer,
        components: SidebarComponents<SidebarComponent>
    ) {
        val teammates = linkedSetOf(player.scoreboardName)
        val team = player.team
        if (team != null) {
            teammates.addAll(team.players)
            components.addRow(SidebarComponent.withNoScore(
                Component.empty()
                    .append(this.buffer)
                    .append(Component.literal("Team: ").bold())
                    .append(Component.literal(team.name).color(team).italicize())
                    .withMiniFont()
            ))
        }

        for (username in teammates) {
            components.addRow(this.createTeammateComponent(player.server, username, team))
        }
    }

    private fun createTeammateComponent(server: MinecraftServer, username: String, team: PlayerTeam?): SidebarComponent {
        val formatted = Component.empty()
            .append(this.buffer)
            .append(this.playerBuffer)
            .append(PixelGridHeadComponents.getHeadOrDefaultFor(DynamicResolvableProfile(username), server))
            .append(" ")
            .append(Component.literal(username).withMiniFont().color(team))
        val teammate = server.player(username)
            ?: return SidebarComponent.withCustomScore(formatted, CasualComponents.Hud.NO_CONNECTION.wrap().append(this.buffer))

        if (!this.health) {
            return SidebarComponent.withCustomScore(formatted.append(this.buffer), Component.empty())
        }
        if (!teammate.isSurvival || !teammate.isAlive) {
            return SidebarComponent.withCustomScore(formatted, CasualComponents.Hud.UNAVAILABLE.wrap().append(this.buffer))
        }
        val health = " %04.1f".format(teammate.health / 2.0)
        val score = Component.literal(health).withMiniFont().append(SpacingFontResources.spaced(1)).append(CasualComponents.Hud.HARDCORE_HEART)
        return SidebarComponent.withCustomScore(formatted, score.append(this.buffer))
    }
}