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
        key: String = Texts.getTranslationKeyOf(foreground)
    ): Pair<LanguageEntry, LanguageEntry> {
        val (first, second) = getCentreSpacingUnicode(font, foreground, background)
        return LanguageEntry("${key}.space.1", first) to LanguageEntry("${key}.space.2", second)
    }

    fun getTranslatableNegativeWidth(
        font: Font,
        component: Component,
        key: String = Texts.getTranslationKeyOf(component)
    ): LanguageEntry {
        val unicode = this.getNegativeWidthUnicode(font, component)
        return LanguageEntry("${key}.negativeWidth", unicode)
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

    fun getNegativeWidthUnicode(font: Font, component: Component): String {
        val width = this.getWidth(font, component)
        val unicode = Texts.space(-width)
        return StringEscapeUtils.escapeJava(unicode.string)
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

    fun getWidth(font: Font, component: Component): Int {
        return font.width(component)
    }
}