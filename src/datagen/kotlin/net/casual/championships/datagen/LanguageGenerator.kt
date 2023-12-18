package net.casual.championships.datagen

import net.casual.championships.utils.LanguageUtils
import net.minecraft.client.Minecraft

class LanguageGenerator(
    private val languages: List<String>
) {
    private val generators = ArrayList<LanguageEntryGenerator>()

    fun add(generator: LanguageEntryGenerator): LanguageGenerator {
        this.generators.add(generator)
        return this
    }

    fun generate(client: Minecraft, consumer: (lang: String, json: String) -> Unit) {
        LanguageUtils.setForeachLanguage(client, this.languages) { lang ->
            val entries = ArrayList<LanguageEntry>()
            for (entry in this.generators) {
                entry.run(client.font, entries)
            }
            consumer(lang, LanguageEntry.toJson(entries))
        }
    }
}