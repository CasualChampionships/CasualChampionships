package net.casual.championships.common

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addFont
import net.casual.arcade.resources.utils.ResourcePackUtils.addLangsFrom
import net.casual.arcade.resources.utils.ResourcePackUtils.addLangsFromData
import net.casual.arcade.resources.utils.ResourcePackUtils.addMissingItemModels
import net.casual.arcade.resources.utils.ResourcePackUtils.addSounds
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.championships.common.items.MenuItem
import net.casual.championships.common.util.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CommonMod: ModInitializer {
    const val MOD_ID = "casual_common"

    private val container = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    val logger: Logger = LoggerFactory.getLogger("CasualCommon")

    val COMMON_PACK = NamedResourcePackCreator.named("common") {
        addAssetSource(container.findPath("packs/common").get())
        addLangsFromData(MOD_ID)
        addLangsFrom("minecraft", container.findPath("data/minecraft/lang").get())
        addFont(CommonComponents.Hud)
        addFont(CommonComponents.Gui)
        addFont(CommonComponents.Text)
        addFont(CommonComponents.Border)
        addSounds(CommonSounds)
        packDescription = "Common resources used in CasualChampionships".literal()
    }

    val CUSTOM_MODEL_PACK = NamedResourcePackCreator.named("common_models") {
        addAssetSource(container.findPath("packs/models").get())
        addMissingItemModels(MOD_ID, container.findPath("packs/common").get())
    }

    val COMMON_PACKS = listOf(
        ArcadeResourcePacks.NO_SHADOW_PACK,
        ArcadeResourcePacks.PADDING_FONT_PACK,
        ArcadeResourcePacks.PLAYER_HEADS_PACK,
        ArcadeResourcePacks.HIDE_PLAYER_LIST_HEADS_PACK,
        ArcadeResourcePacks.MINI_MINECRAFT_FONT,
        ArcadeResourcePacks.SPACES_FONT_PACK,
        ArcadeResourcePacks.HIDE_PLAYER_LIST_PING_PACK,
        ArcadeResourcePacks.ACTION_BAR_FONT_PACK,
        ArcadeResourcePacks.MINI_ACTION_BAR_FONT_PACK,
        CUSTOM_MODEL_PACK,
        COMMON_PACK
    )

    override fun onInitialize() {
        CommonItems.noop()
        CommonEntities.noop()
        CommonSounds.noop()

        AntiCheat.registerEvents()

        PolymerItemGroupUtils.registerPolymerItemGroup(
            id("menu"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                .title("Menu Items".literal())
                .icon(MenuItem::TICK)
                .alignedRight()
                .displayItems { _, output ->
                    output.acceptAll(MenuItem.MODELLER.all())
                }
                .build()
        )
        PolymerItemGroupUtils.registerPolymerItemGroup(
            id("heads"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                .title("Head Items".literal())
                .icon(CommonItems.GOLDEN_HEAD::getDefaultInstance)
                .alignedRight()
                .displayItems { _, output ->
                    output.accept(CommonItems.GOLDEN_HEAD)
                    output.accept(CommonItems.PLAYER_HEAD)
                    output.accept(CommonItems.FORWARD_FACING_PLAYER_HEAD)
                }
                .build()
        )
    }

    fun id(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }
}