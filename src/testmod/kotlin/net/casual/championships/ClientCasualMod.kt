package net.casual.championships

import net.casual.arcade.utils.CommandUtils.commandSuccess
import net.casual.championships.util.Texts
import net.casual.championships.util.Texts.monospaced
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.resources.language.ClientLanguage
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import org.apache.commons.text.StringEscapeUtils

class ClientCasualMod: ClientModInitializer {
    companion object {
        private const val ENGLISH = "en_us"
    }

    override fun onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommandManager.literal("spacing").executes {
                    this.run(it.source.client).commandSuccess()
                }
            )
        }
    }

    private fun run(client: Minecraft) {
        val languages = listOf(ENGLISH, "es_es")

        for (code in languages) {
            val bidirectional = client.languageManager.getLanguage(code)?.bidirectional ?: false
            val fallbacks = if (code == ENGLISH) listOf(ENGLISH) else listOf(ENGLISH, code)
            val language = ClientLanguage.loadFrom(client.resourceManager, fallbacks, bidirectional)
            Language.inject(language)

            runForLanguage(client)
        }
    }

    private fun runForLanguage(client: Minecraft) {
        val (first, second) = getTranslatableCentreSpacing(
            client.font,
            Component.translatable("uhc.bossbar.starting", "00:00:00").monospaced(),
            Texts.ICON_WIDE_BACKGROUND
        )
        println(first)
        println(second)
    }

    private fun getTranslatableCentreSpacing(
        font: Font,
        foreground: Component,
        background: Component,
        key: String = extractTranslationKey(foreground)
    ): Pair<String, String> {
        val (first, second) = getCentreSpacing(font, foreground, background)
        return """"${key}.space.1": "$first"""" to """"${key}.space.2": "$second""""
    }

    private fun getCentreSpacing(
        font: Font,
        foreground: Component,
        background: Component
    ): Pair<String, String> {
        val foregroundWidth = font.width(foreground)
        val backgroundWidth = font.width(background)
        val emptySpace = backgroundWidth - foregroundWidth
        val halfSpace = emptySpace / 2

        val firstSpace = -halfSpace
        val secondSpace = -backgroundWidth + halfSpace

        val first = Texts.space(firstSpace)
        val second = Texts.space(secondSpace)

        return StringEscapeUtils.escapeJava(first.string) to StringEscapeUtils.escapeJava(second.string)
    }

    private fun extractTranslationKey(component: Component): String {
        val contents = component.contents
        if (contents !is TranslatableContents) {
            throw IllegalStateException()
        }
        return contents.key
    }
}