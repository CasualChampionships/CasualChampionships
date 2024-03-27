package net.casual.championships.common.datagen

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.datagen.language.CentredSpacingGenerator
import net.casual.arcade.datagen.language.LanguageGenerator
import net.casual.arcade.datagen.language.NegativeWidthGenerator
import net.casual.arcade.datagen.resource.ArcadeResourceGenerator
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.championships.common.CommonMod
import net.casual.championships.common.util.CommonComponents
import net.minecraft.client.Minecraft
import net.minecraft.core.Direction8

class CommonDatagen: ArcadeResourceGenerator {
    companion object {
        private val SUPPORTED_LANGUAGES = listOf("en_us")
    }

    override fun id(): String {
        return CommonMod.MOD_ID
    }

    override fun resources(): Collection<ResourcePackCreator> {
        return CommonMod.COMMON_PACKS.map(NamedResourcePackCreator::getCreator)
    }

    override fun run(client: Minecraft) {
        val generator = LanguageGenerator(SUPPORTED_LANGUAGES).apply {
            add(CentredSpacingGenerator(
                CommonComponents.STARTING_IN_MESSAGE.generate("00:00:00").mini(),
                CommonComponents.Bitmap.BACKGROUND_240
            ))
            add(CentredSpacingGenerator(
                CommonComponents.TIME_ELAPSED_MESSAGE.generate("00:00:00").mini(),
                CommonComponents.Bitmap.BACKGROUND_240
            ))
            add(CentredSpacingGenerator(
                CommonComponents.GRACE_MESSAGE.generate("00:00").mini(),
                CommonComponents.Bitmap.BACKGROUND_180
            ))
            add(CentredSpacingGenerator(
                CommonComponents.GLOWING_MESSAGE.generate("00:00").mini(),
                CommonComponents.Bitmap.BACKGROUND_180
            ))
            add(CentredSpacingGenerator(
                CommonComponents.STARTING_SOON_MESSAGE.mini(),
                CommonComponents.Bitmap.BACKGROUND_240
            ))

            for (direction in Direction8.values()) {
                add(NegativeWidthGenerator(CommonComponents.direction(direction)))
            }
        }

        try {
            generator.replaceLangs(client, this.getDataPath().resolve("lang"))
        } catch (e: Throwable) {
            CommonMod.logger.error("Failed to replace lags", e)
        }
    }
}