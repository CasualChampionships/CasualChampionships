package net.casual.championships.common.ui

import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.gui.sidebar.SidebarSupplier
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.PlayerUtils.distanceToNearestBorder
import net.casual.championships.common.util.CommonComponents
import net.minecraft.ChatFormatting.*
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class BorderDistanceRow(private val buffer: Component): SidebarSupplier {
    override fun getComponent(player: ServerPlayer): SidebarComponent {
        val vectorToBorder = player.distanceToNearestBorder()
        val multiplier = if (vectorToBorder.x < 0 || vectorToBorder.z < 0) -1 else 1
        val distanceToBorder =  multiplier * vectorToBorder.length().toInt()

        val percent = distanceToBorder / (player.level().worldBorder.size / 2.0)
        val colour = if (percent > 0.4) DARK_GREEN else if (percent > 0.2) YELLOW else if (percent > 0.1) RED else DARK_RED
        val display = Component.empty().append(this.buffer).append(CommonComponents.BORDER_DISTANCE_MESSAGE)
        val score = distanceToBorder.toString().literal().append(this.buffer).withStyle(colour)
        return SidebarComponent.withCustomScore(display, score)
    }
}