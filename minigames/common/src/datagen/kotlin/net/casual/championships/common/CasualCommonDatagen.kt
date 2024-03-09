package net.casual.championships.common

import net.casual.arcade.datagen.language.CentredSpacingGenerator
import net.casual.arcade.datagen.language.LanguageGenerator
import net.casual.arcade.datagen.language.NegativeWidthGenerator
import net.casual.arcade.resources.ArcadePacks
import net.casual.championships.common.util.CommonComponents
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.minecraft.client.Minecraft
import net.minecraft.core.Direction8
import java.util.concurrent.CompletableFuture
import kotlin.io.path.Path

class CasualCommonDatagen: ClientModInitializer {
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
                CommonComponents.STARTING_IN_MESSAGE.generate("00:00:00"),
                CommonComponents.BACKGROUND_240_BITMAP
            ))
            add(CentredSpacingGenerator(
                CommonComponents.TIME_ELAPSED_MESSAGE.generate("00:00:00"),
                CommonComponents.BACKGROUND_240_BITMAP
            ))
            add(CentredSpacingGenerator(
                CommonComponents.GRACE_MESSAGE.generate("00:00"),
                CommonComponents.BACKGROUND_180_BITMAP
            ))
            add(CentredSpacingGenerator(
                CommonComponents.GLOWING_MESSAGE.generate("00:00"),
                CommonComponents.BACKGROUND_180_BITMAP
            ))
            add(CentredSpacingGenerator(
                CommonComponents.STARTING_SOON_MESSAGE,
                CommonComponents.BACKGROUND_240_BITMAP
            ))

            for (direction in Direction8.values()) {
                add(NegativeWidthGenerator(CommonComponents.direction(direction)))
            }
        }

        // Only meant to be run in dev
        try {
            generator.replaceLangs(client, Path("../src/main/resources/data/casual_common/lang/"))
        } catch (e: Throwable) {
            CasualCommonMod.logger.error("Failed to replace lags", e)
        }
    }

    private fun loadPack(client: Minecraft): CompletableFuture<Void> {
        val packs = listOf(ArcadePacks.SPACES_FONT_PACK, CasualCommonMod.COMMON_PACK)
        for (pack in packs) {
            pack.buildTo(client.resourcePackDirectory)
        }
        client.resourcePackRepository.reload()
        for (pack in packs) {
            client.resourcePackRepository.addPack("file/${pack.zippedName()}")
        }
        return client.reloadResourcePacks()
    }
}