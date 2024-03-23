package net.casual.championships.resources

import net.casual.arcade.resources.ArcadePacks
import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.casual.championships.CasualMod
import net.casual.championships.common.CasualCommonMod
import net.casual.championships.common.item.CommonItems
import net.casual.championships.uhc.UHCMod
import net.casual.championships.util.Config

object CasualResourcePack {
    private val default = listOf(
        ArcadePacks.NO_SHADOW_PACK,
        ArcadePacks.SPACES_FONT_PACK,
        ArcadePacks.MINI_MINECRAFT_FONT,
        ArcadePacks.ACTION_BAR_FONT_PACK,
        ArcadePacks.PLAYER_HEADS_PACK,
        ArcadePacks.PADDING_FONT_PACK,
        ArcadePacks.HIDE_PLAYER_LIST_HEADS_PACK,
        ArcadePacks.HIDE_PLAYER_LIST_PING_PACK,
        CasualCommonMod.COMMON_PACK,
        CommonItems.CUSTOM_MODEL_PACK
    )

    private val all = default.concat(listOf(UHCMod.UHC_PACK))

    fun generate() {
        for (pack in this.all) {
            try {
                pack.buildTo(Config.resolve("packs"))
            } catch (e: Exception) {
                CasualMod.logger.error("Failed to build pack ${pack.zippedName()}", e)
            }
        }
    }

    fun getPackNames(): List<String> {
        return this.default.map { it.zippedName() }
    }
}