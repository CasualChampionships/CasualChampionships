package net.casual.championships


import net.casual.championships.datagen.CentredSpacingGenerator
import net.casual.championships.datagen.LanguageGenerator
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.util.Texts
import net.casual.championships.util.Texts.monospaced
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture

class CasualDatagen: ClientModInitializer {

    override fun onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register { client ->
            this.loadPack(client).thenAcceptAsync({
                this.run(client)

                // We don't really want to boot into the game...
                client.stop()
            }, client)
        }
    }

    private fun run(client: Minecraft) {
        val generator = LanguageGenerator(listOf("en_us", "es_es")).apply {
            add(CentredSpacingGenerator(
                Component.translatable("uhc.bossbar.starting", "00:00:00"),
                Texts.ICON_WIDE_BACKGROUND
            ))
            add(CentredSpacingGenerator(
                Component.translatable("uhc.bossbar.elapsed", "00:00:00"),
                Texts.ICON_WIDE_BACKGROUND
            ))
            add(CentredSpacingGenerator(
                Component.translatable("uhc.bossbar.grace", "00:00"),
                Texts.ICON_BACKGROUND
            ))
            add(CentredSpacingGenerator(
                Component.translatable("uhc.bossbar.glowing", "00:00"),
                Texts.ICON_BACKGROUND
            ))
            add(CentredSpacingGenerator(
                Component.translatable("uhc.bossbar.startingSoon"),
                Texts.ICON_WIDE_BACKGROUND
            ))
        }

        generator.generate(client) { lang, json ->
            println(lang)
            println(json)
        }
    }

    private fun loadPack(client: Minecraft): CompletableFuture<Void> {
        CasualResourcePack.initialise()
        CasualResourcePack.generate(client.resourcePackDirectory.resolve("test.zip"))
        client.resourcePackRepository.reload()
        client.resourcePackRepository.addPack("file/test.zip")
        return client.reloadResourcePacks()
    }
}