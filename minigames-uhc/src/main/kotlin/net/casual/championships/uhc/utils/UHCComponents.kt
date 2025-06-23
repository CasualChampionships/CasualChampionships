package net.casual.championships.uhc.utils

import net.casual.arcade.resources.font.FontResources
import net.casual.championships.uhc.UHCMod

object UHCComponents {
    object Bitmap: FontResources(UHCMod.id("bitmap_font")) {
        val TITLE = bitmap(at("uhc_title.png"), 8, 9)

        val PLAYER_BACKGROUND = bitmap(at("player_background.png"), 9, 10)
    }
}