package net.casual.championships.minigame.uhc

import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.gui.sidebar.SidebarSupplier
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.championships.minigame.uhc.gui.BorderDistanceRow
import net.casual.championships.minigame.uhc.gui.TeammateRow
import net.casual.championships.util.Texts
import net.minecraft.network.chat.Component

object UHCUtils {
    fun createSidebar(teamSize: Int): ArcadeSidebar {
        val sidebar = ArcadeSidebar(ComponentSupplier.of(Texts.CASUAL_UHC.gold().bold()))

        val buffer = Texts.space()
        val teammates = Component.empty().append(buffer).append(Texts.SIDEBAR_TEAMMATES)
        sidebar.addRow(SidebarSupplier.withNoScore(teammates))
        for (i in 0 until teamSize) {
            sidebar.addRow(TeammateRow(i, buffer))
        }
        sidebar.addRow(SidebarSupplier.empty())
        sidebar.addRow(BorderDistanceRow(buffer))
        sidebar.addRow { player ->
            val display = Component.empty().append(buffer).append(Texts.UHC_WB_RADIUS)
            val score = (player.level().worldBorder.size / 2.0).toInt().toString().literal().append(buffer)
            SidebarComponent.withCustomScore(display, score)
        }
        sidebar.addRow(SidebarSupplier.empty())
        return sidebar
    }
}