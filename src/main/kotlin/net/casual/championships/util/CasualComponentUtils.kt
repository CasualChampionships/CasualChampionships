package net.casual.championships.util

import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.string.SmallCapsTitleCase
import net.casual.arcade.utils.string.TitleCase
import net.casual.arcade.utils.string.convertCasing
import net.casual.arcade.utils.string.toSmallCaps
import net.minecraft.network.chat.Component

object CasualComponentUtils {
    fun getMessageOfTheDay(): Component {
        return Component {
            val cc = literal("Casual Championships".convertCasing(TitleCase, SmallCapsTitleCase)).bold().color(0xFFAC1C)
            val title = literal("\uD83D\uDDE1").yellow() + " " + cc + " " + literal("\uD83C\uDFF9").yellow()
            val subtitle = literal("   be prepared".toSmallCaps()).lime() + wrap() +
                literal(" ◆ ").white() + literal("let the chaos ensue    ".toSmallCaps()).lime()
            empty() + literal("╔═════", 0x009BFF, 0x80CDFF) + title + literal("═════╗", 0x80CDFF, 0x009BFF) + nl +
                literal("╚══", 0x009BFF, 0x33AFFF) + subtitle + literal("══╝", 0x33AFFF, 0x009BFF)
        }
    }
}