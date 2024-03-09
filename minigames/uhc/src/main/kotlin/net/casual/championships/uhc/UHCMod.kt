package net.casual.championships.uhc

import net.casual.arcade.resources.NamedResourcePackCreator
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ResourcePackUtils.addLangsFromData
import net.fabricmc.api.DedicatedServerModInitializer
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object UHCMod: DedicatedServerModInitializer {
    const val MOD_ID = "casual_uhc"

    val logger: Logger = LoggerFactory.getLogger("CasualUHC")

    val UHC_PACK = NamedResourcePackCreator.named("uhc") {
        addAssetSource(MOD_ID)
        addLangsFromData(MOD_ID)
        packDescription = "Resources for CasualChampionships UHC minigame".literal()
    }

    override fun onInitializeServer() {

    }

    fun id(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }
}