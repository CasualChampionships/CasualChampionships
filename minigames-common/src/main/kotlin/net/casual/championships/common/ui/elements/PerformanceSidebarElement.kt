package net.casual.championships.common.ui.elements

import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.arcade.virtual.visuals.utils.elements.component.MSPTComponentElement
import net.casual.arcade.virtual.visuals.utils.elements.component.TPSComponentElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

class PerformanceSidebarElement(private val buffer: Component): UniversalElement<SidebarComponent> {
    override fun get(server: MinecraftServer): SidebarComponent {
        val tps = Component.empty().append(this.buffer).append(TPSComponentElement.get(server)).withMiniFont()
        val mspt = Component.empty().append(MSPTComponentElement.get(server)).append(this.buffer).withMiniFont()
        return SidebarComponent.withCustomScore(tps, mspt)
    }
}