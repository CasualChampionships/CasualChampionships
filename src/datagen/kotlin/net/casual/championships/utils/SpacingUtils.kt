package net.casual.championships.utils

import net.casual.championships.datagen.LanguageEntry
import net.casual.championships.util.Texts
import net.minecraft.client.gui.Font
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.util.Mth
import org.apache.commons.text.StringEscapeUtils

object SpacingUtils {
    fun getTranslatableCentreSpacing(
        font: Font,
        foreground: Component,
        background: Component,
        key: String = extractTranslationKey(foreground)
    ): Pair<LanguageEntry, LanguageEntry> {
        val (first, second) = getCentreSpacingUnicode(font, foreground, background)
        return LanguageEntry("${key}.space.1", first) to LanguageEntry("${key}.space.2", second)
    }

    fun getCentreSpacingUnicode(
        font: Font,
        foreground: Component,
        background: Component
    ): Pair<String, String> {
        val (firstSpace, secondSpace) = getCentreSpacing(font, foreground, background)
        val first = Texts.space(firstSpace)
        val second = Texts.space(secondSpace)

        return StringEscapeUtils.escapeJava(first.string) to StringEscapeUtils.escapeJava(second.string)
    }

    fun getCentreSpacing(
        font: Font,
        foreground: Component,
        background: Component
    ): Pair<Int, Int> {
        val foregroundWidth = font.width(foreground)
        val backgroundWidth = font.width(background)
        val emptySpace = backgroundWidth - foregroundWidth
        val halfSpace = emptySpace / 2.0
        val halfSpaceFloor = Mth.floor(halfSpace)
        val halfSpaceCeil = Mth.ceil(halfSpace)

        val firstSpace = -halfSpaceFloor
        val secondSpace = -backgroundWidth + halfSpaceCeil
        return firstSpace to secondSpace
    }

    private fun extractTranslationKey(component: Component): String {
        val contents = component.contents
        if (contents !is TranslatableContents) {
            throw IllegalStateException()
        }
        return contents.key
    }
}