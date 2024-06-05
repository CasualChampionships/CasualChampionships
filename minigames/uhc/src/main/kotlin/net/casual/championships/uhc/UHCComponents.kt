package net.casual.championships.uhc

import net.casual.arcade.resources.font.FontResources
import net.casual.arcade.utils.ComponentUtils.translatable

object UHCComponents {
    val BROADCAST_SPECTATING by translatable("uhc.broadcast.spectator")

    object Bitmap: FontResources(UHCMod.id("bitmap_font")) {
        val TITLE by bitmap(at("uhc_title.png"), 8, 9)
    }
}