package net.casual.championships.resources

import net.casual.arcade.resources.ArcadePacks
import net.casual.arcade.resources.NamedResourcePackCreator
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.championships.CasualMod
import net.casual.championships.common.CasualCommonMod
import net.casual.championships.common.item.CasualCommonItems
import net.casual.championships.uhc.UHCMod
import net.casual.championships.util.Config
import java.nio.file.Path
import kotlin.io.path.readBytes

object CasualResourcePack {
    private val CHAMPIONSHIPS_PACK = NamedResourcePackCreator.named("casual_championships") {
        addAssetSource(CasualMod.ID)
        packDescription = "Provides resources for Casual Championships".literal()
        val container = CasualMod.container
        val path = container.findPath(container.metadata.getIconPath(64).get()).get()
        packIcon = path.readBytes()
    }

    private val packs = listOf(
        ArcadePacks.NO_SHADOW_PACK,
        ArcadePacks.SPACES_FONT_PACK,
        ArcadePacks.ACTION_BAR_FONT_PACK,
        CasualCommonMod.COMMON_PACK,
        CasualCommonItems.CUSTOM_MODEL_PACK,
        UHCMod.UHC_PACK,
        CHAMPIONSHIPS_PACK
    )

    fun generate() {
        for (pack in this.packs) {
            try {
                pack.buildTo(Config.resolve("packs"))
            } catch (e: Exception) {
                CasualMod.logger.error("Failed to build pack ${pack.zippedName()}", e)
            }
        }
    }
}