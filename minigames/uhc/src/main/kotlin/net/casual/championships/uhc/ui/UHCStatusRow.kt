package net.casual.championships.uhc.ui

import net.casual.arcade.gui.elements.UniversalElement
import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.championships.uhc.UHCMinigame
import net.casual.championships.uhc.UHCPhase
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

class UHCStatusRow(private val uhc: UHCMinigame): UniversalElement<SidebarComponent> {
    // TODO: Clean up and translations
    override fun get(server: MinecraftServer): SidebarComponent {
        val status = when (this.uhc.phase) {
            UHCPhase.Initializing -> Component.literal("Initializing ")
            UHCPhase.Grace -> Component.literal("Grace ")
            UHCPhase.BorderMoving -> {
                if (this.uhc.bordersMoving) {
                    Component.literal("Borders Moving ")
                } else {
                    Component.literal("Borders Paused ")
                }
            }
            UHCPhase.BorderFinished -> Component.literal("Border Finished ")
            else -> Component.literal("Game Over ")
        }
        return SidebarComponent.withCustomScore(Component.literal(" Status:").mini(), status.mini())
    }
}