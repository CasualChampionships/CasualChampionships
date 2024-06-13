package net.casual.championships.util

import com.google.common.collect.HashBiMap
import net.casual.MinecraftColor
import net.minecraft.ChatFormatting

object DataUtils {
    private val colors = HashBiMap.create<ChatFormatting, MinecraftColor>()

    init {
        colors[ChatFormatting.BLACK] = MinecraftColor.BLACK
        colors[ChatFormatting.DARK_BLUE] = MinecraftColor.DARK_BLUE
        colors[ChatFormatting.DARK_GREEN] = MinecraftColor.DARK_GREEN
        colors[ChatFormatting.DARK_AQUA] = MinecraftColor.DARK_AQUA
        colors[ChatFormatting.DARK_RED] = MinecraftColor.DARK_RED
        colors[ChatFormatting.DARK_PURPLE] = MinecraftColor.DARK_PURPLE
        colors[ChatFormatting.GOLD] = MinecraftColor.GOLD
        colors[ChatFormatting.GRAY] = MinecraftColor.GRAY
        colors[ChatFormatting.DARK_GRAY] = MinecraftColor.DARK_GRAY
        colors[ChatFormatting.BLUE] = MinecraftColor.BLUE
        colors[ChatFormatting.GREEN] = MinecraftColor.GREEN
        colors[ChatFormatting.AQUA] = MinecraftColor.AQUA
        colors[ChatFormatting.RED] = MinecraftColor.RED
        colors[ChatFormatting.LIGHT_PURPLE] = MinecraftColor.LIGHT_PURPLE
        colors[ChatFormatting.YELLOW] = MinecraftColor.YELLOW
        colors[ChatFormatting.WHITE] = MinecraftColor.WHITE
    }

    fun ChatFormatting.toMinecraftColor(): MinecraftColor {
        return colors[this] ?: MinecraftColor.WHITE
    }

    fun MinecraftColor.toChatFormatting(): ChatFormatting {
        return colors.inverse()[this] ?: ChatFormatting.WHITE
    }
}