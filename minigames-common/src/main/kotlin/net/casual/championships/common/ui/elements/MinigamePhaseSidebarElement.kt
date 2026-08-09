package net.casual.championships.common.ui.elements

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.component.green
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

class MinigamePhaseSidebarElement(
    private val minigame: Minigame,
    private val buffer: Component
): UniversalElement<SidebarComponent> {
    override fun get(server: MinecraftServer): SidebarComponent {
        return SidebarComponent.withCustomScore(
            Component.empty().append(this.buffer).append("Phase:").withMiniFont(),
            Component.literal(this.minigame.phase.id).append(this.buffer).green().withMiniFont()
        )
    }
}