package net.casual.championships.uhc.ui

import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.pack.utils.spaced
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.pack.utils.withMiniShiftedDownFont
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.component.Component
import net.casual.arcade.utils.component.plus
import net.casual.arcade.virtual.visuals.sidebar.DynamicVirtualSidebar
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponents
import net.casual.championships.common.ui.elements.TeammatesSidebarElements
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils
import net.casual.championships.uhc.minigame.GAME_OVER_ID
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.ui.elements.BoundaryMovingElement
import net.casual.championships.uhc.ui.elements.MinigamePhaseSidebarElement
import net.casual.championships.uhc.ui.elements.MobcapSidebarElement
import net.casual.championships.uhc.ui.elements.PerformanceSidebarElement
import net.casual.championships.uhc.utils.UHCComponents
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.server.level.ServerPlayer

class UHCHud(
    private val uhc: UHCMinigame
): MinigameEventListener {
    fun createSidebar(): DynamicVirtualSidebar {
        val sidebar = DynamicVirtualSidebar(this.uhc.server)
        sidebar.title.set(UHCComponents.Bitmap.TITLE)

        val buffer = SpacingFontResources.spaced(4)
        val border = CasualGuiUtils.getBorderSidebarElements(buffer)
        val pause = BoundaryMovingElement(this.uhc.boundary, buffer).cached()
        val teammates = TeammatesSidebarElements(Component.empty(), buffer, true)
        val performance = PerformanceSidebarElement(SpacingFontResources.spaced(2)).cached()
        val phase = MinigamePhaseSidebarElement(this.uhc, SpacingFontResources.spaced(2)).cached()
        val mobcaps = MobcapSidebarElement.cached()

        border.forEach(sidebar::addTickable)
        sidebar.addTickable(pause)
        sidebar.addTickable(performance)
        sidebar.addTickable(phase)
        sidebar.addTickable(mobcaps)

        sidebar.setRows { player ->
            val components = SidebarComponents.empty()

            val team = player.team
            if (team != null && !this.uhc.teams.isAdminTeam(team) && !this.uhc.teams.isSpectatorTeam(team)) {
                teammates.addTeammates(player, components)
                components.addRow(SidebarComponent.EMPTY)
            }

            if (this.uhc.players.isAdmin(player)) {
                components.addRow(performance.get(player))
                components.addRow(phase.get(player))
                components.addRow(SidebarComponent.EMPTY)
                components.addRow(Component { empty() + spaced(2.0F) + "Mobcaps:" }.withMiniFont())
                components.addRow(mobcaps.get(player))
                components.addRow(SidebarComponent.EMPTY)
            }

            if (components.size() == 0) {
                components.addRow(SidebarComponent.EMPTY)
            }
            border.forEach { components.addRow(it.get(player)) }
            components.addRow(pause.get(player))
            components.addRow(SidebarComponent.EMPTY)
        }
        return sidebar
    }

    @Listener(during = During(before = GAME_OVER_ID))
    private fun onPlayerTick(event: PlayerTickEvent) {
        this.updateActionBar(event.player)
    }

    private fun updateActionBar(player: ServerPlayer) {
        val direction = CasualComponents.direction(Direction.orderedByNearest(player).filter { it.axis != Direction.Axis.Y }[0])
        val position = "(${player.blockX}, ${player.blockY}, ${player.blockZ})"
        val shift = position.length - 7

        val mode = this.uhc.chat.getChatModeFor(player)
        player.connection.send(ClientboundSetActionBarTextPacket(
            @Suppress("Deprecation")
            Component {
                empty() + spaced(26.0F) + spaced(shift * 6.0F) + ComponentUtils.negativeWidthOf(mode.name) +
                    spaced(-11.0F) + CasualComponents.Hud.EPIC_CHAT_ICON_M54 + spaced(1.0F) +
                    (empty() + mode.name).withMiniShiftedDownFont(63) +
                    spaced(190.0F) + direction.withMiniShiftedDownFont(54) +
                    ComponentUtils.negativeWidthOf(direction) + spaced(-2.0F) +
                    Component.literal(position).withMiniShiftedDownFont(63)
            }
        ))
    }
}
