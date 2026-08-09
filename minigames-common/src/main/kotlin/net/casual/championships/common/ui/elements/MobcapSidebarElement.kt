package net.casual.championships.common.ui.elements

import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.virtual.visuals.elements.LevelSpecificElement
import net.casual.arcade.virtual.visuals.sidebar.SidebarComponent
import net.casual.arcade.virtual.visuals.utils.elements.component.MobcapComponentElement
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel

object MobcapSidebarElement: LevelSpecificElement<SidebarComponent> {
    override fun get(level: ServerLevel): SidebarComponent {
        return SidebarComponent.withNoScore(Component.empty().append(MobcapComponentElement.get(level)).withMiniFont())
    }
}