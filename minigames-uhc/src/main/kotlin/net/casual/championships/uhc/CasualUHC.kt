package net.casual.championships.uhc

import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addFont
import net.casual.arcade.resources.utils.ResourcePackUtils.addLangsFromData
import net.casual.arcade.resources.utils.ResourcePackUtils.addMissingItemModels
import net.casual.championships.uhc.gui.UHCMapRenderer
import net.casual.championships.uhc.item.UHCItems
import net.casual.championships.uhc.utils.UHCComponents
import net.casual.championships.uhc.utils.UHCStats
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CasualUHC: ModInitializer {
    const val MOD_ID = "casual_uhc"

    internal val container = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    internal val logger: Logger = LoggerFactory.getLogger("CasualUHC")

    val UHC_PACK = NamedResourcePackCreator.named("uhc") {
        addAssetSource(MOD_ID)
        addLangsFromData(MOD_ID)
        addMissingItemModels(MOD_ID)
        addFont(UHCComponents.Bitmap)
        packDescription = Component.literal("Resources for CasualChampionships UHC minigame")
    }

    override fun onInitialize() {
        UHCMapRenderer.load()
        UHCItems.load()
        UHCStats.load()
    }
}