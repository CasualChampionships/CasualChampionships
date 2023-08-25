package net.casual.minigame.uhc.gui

import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.utils.PlayerUtils.distanceToNearestBorder
import net.casual.util.Texts
import net.casual.util.Texts.monospaced
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class BorderDistanceRow(val buffer: String): ComponentSupplier {
    override fun getComponent(player: ServerPlayer): Component {
        val vectorToBorder = player.distanceToNearestBorder()
        val multiplier = if (vectorToBorder.x < 0 || vectorToBorder.z < 0) -1 else 1
        val distanceToBorder =  multiplier * vectorToBorder.length().toInt()

        val percent = distanceToBorder / (player.level().worldBorder.size / 2.0)
        val colour = if (percent > 0.4) ChatFormatting.DARK_GREEN else if (percent > 0.2) ChatFormatting.YELLOW else if (percent > 0.1) ChatFormatting.RED else ChatFormatting.DARK_RED
        return Component.literal(this.buffer).append(Texts.UHC_DISTANCE_TO_WB.generate(Component.literal(distanceToBorder.toString()).withStyle(colour))).monospaced()
    }
}