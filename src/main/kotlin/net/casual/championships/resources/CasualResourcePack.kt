package net.casual.championships.resources

import net.casual.arcade.utils.impl.ConcatenatedList.Companion.concat
import net.casual.championships.CasualMod
import net.casual.championships.common.CommonMod
import net.casual.championships.uhc.UHCMod
import net.casual.championships.util.Config
import java.nio.file.Path

object CasualResourcePack {
    private val default = CommonMod.COMMON_PACKS.toList()

    // TODO: make this autogen
    private val all = default.concat(listOf(UHCMod.UHC_PACK))

    fun generateAll(location: Path = Config.resolve("packs")) {
        for (pack in this.all) {
            try {
                pack.buildTo(location)
            } catch (e: Exception) {
                CasualMod.logger.error("Failed to build pack ${pack.zippedName()}", e)
            }
        }
    }
}