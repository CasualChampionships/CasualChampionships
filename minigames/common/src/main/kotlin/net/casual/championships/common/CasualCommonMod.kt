package net.casual.championships.common

import net.casual.arcade.resources.NamedResourcePackCreator
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ResourcePackUtils.addLangsFromData
import net.casual.championships.common.item.CasualCommonItems
import net.casual.championships.common.util.AntiCheat
import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CasualCommonMod: DedicatedServerModInitializer {
    const val MOD_ID = "casual_common"

    val logger: Logger = LoggerFactory.getLogger("CasualCommon")

    val COMMON_PACK = NamedResourcePackCreator.named("common") {
        addAssetSource(MOD_ID)
        addLangsFromData(MOD_ID)
        packDescription = "Common resources used in CasualChampionships".literal()
    }

    override fun onInitializeServer() {
        CasualCommonItems.noop()

        AntiCheat.registerEvents()
    }

    fun id(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }
}