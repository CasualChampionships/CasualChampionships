package net.casual.championships.common.datagen

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.arcade.datagen.language.CentredSpacingGenerator
import net.casual.arcade.datagen.language.LanguageGenerator
import net.casual.arcade.datagen.language.NegativeWidthGenerator
import net.casual.arcade.datagen.language.WidthDifferenceGenerator
import net.casual.arcade.datagen.resource.ArcadeResourceGenerator
import net.casual.arcade.minigame.managers.chat.MinigameChatMode.*
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.championships.common.CasualCommon
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualUtils
import net.minecraft.client.Minecraft
import net.minecraft.core.Direction8
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
class CommonDatagen: ArcadeResourceGenerator {
    companion object {
        private val SUPPORTED_LANGUAGES = listOf("en_us")
    }

    override fun id(): String {
        return CasualCommon.MOD_ID
    }

    override fun resources(): Collection<ResourcePackCreator> {
        return CasualCommon.COMMON_PACKS.map(NamedResourcePackCreator::getCreator)
    }

    override fun run(client: Minecraft) {
        val generator = LanguageGenerator(SUPPORTED_LANGUAGES).apply {
            add(CentredSpacingGenerator(
                CasualComponents.STARTING_IN.generate("00:00:00").withMiniFont(),
                CasualComponents.Hud.BACKGROUND_240
            ))
            add(CentredSpacingGenerator(
                CasualComponents.TIME_ELAPSED.generate("00:00:00").withMiniFont(),
                CasualComponents.Hud.BACKGROUND_240
            ))
            add(CentredSpacingGenerator(
                CasualComponents.GRACE.generate("00:00").withMiniFont(),
                CasualComponents.Hud.BACKGROUND_180
            ))
            add(CentredSpacingGenerator(
                CasualComponents.GLOWING.generate("00:00").withMiniFont(),
                CasualComponents.Hud.BACKGROUND_180
            ))
            add(CentredSpacingGenerator(
                CasualComponents.STARTING_SOON.withMiniFont(),
                CasualComponents.Hud.BACKGROUND_240
            ))
            add(WidthDifferenceGenerator(
                CasualComponents.SPECTATORS.withMiniFont(),
                CasualComponents.ADMINS.withMiniFont()
            ))

            for (direction in Direction8.entries) {
                add(NegativeWidthGenerator(CasualComponents.direction(direction)))
            }

            val modes = listOf(Global, Admin, Spectator, OwnTeam).map { it.name.copy().withMiniFont() }
            for (mode in modes) {
                add(NegativeWidthGenerator(mode))
            }
        }

        try {
            generator.replaceLangs(client, this.getDataPath().resolve("lang"))
        } catch (e: Throwable) {
            CasualUtils.logger.error("Failed to replace lags", e)
        }
    }
}