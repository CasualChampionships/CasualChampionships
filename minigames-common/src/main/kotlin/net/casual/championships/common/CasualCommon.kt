package net.casual.championships.common

import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.pack.generation.BuiltInResourcePacks
import net.casual.arcade.pack.generation.PackDefinition
import net.casual.championships.common.anticheat.CasualAntiCheat
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.util.*
import net.casual.championships.common.util.level.ReducedMobSpawningRules
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab

object CasualCommon: ModInitializer {
    const val MOD_ID = "casual-common"

    private val container = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    private val COMMON_PACK = PackDefinition("common") {
        description = Component.literal("Common resources used in CasualChampionships")
        include(container)
        addLangs(MOD_ID, container)
        addFont(CasualComponents.Hud)
        addFont(CasualComponents.Gui)
        addFont(CasualComponents.Text)
        addFont(CasualComponents.Border)
        addSounds(CasualSounds)
        generateMissingItemModels(CasualUtils.MOD_ID, container)
    }

    val COMMON_PACKS = listOf(
        BuiltInResourcePacks.PADDING_FONT_PACK,
        BuiltInResourcePacks.PIXEL_FONT_PACK,
        BuiltInResourcePacks.HIDE_PLAYER_LIST_HEADS_PACK,
        BuiltInResourcePacks.MINI_MINECRAFT_FONT_PACK,
        BuiltInResourcePacks.SPACING_FONT_PACK,
        BuiltInResourcePacks.HIDE_PLAYER_LIST_PING_PACK,
        BuiltInResourcePacks.ACTION_BAR_FONT_PACK,
        BuiltInResourcePacks.MINI_ACTION_BAR_FONT_PACK,
        BuiltInResourcePacks.ARCADE_LANG_PACK,
        COMMON_PACK
    )

    override fun onInitialize() {
        CasualItems.load()
        CasualSounds.load()
        CasualStats.load()

        CasualAntiCheat.registerEvents()

        this.registerSpawningRules()
        this.registerItemGroups()
    }

    private fun registerSpawningRules() {
        Registry.register(
            DimensionRegistries.CUSTOM_MOB_SPAWNING_RULES, casual("reduced_mob_cap"), ReducedMobSpawningRules
        )
    }

    private fun registerItemGroups() {
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(
            casual("menu"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                .title(Component.literal("Display Items"))
                .icon(CasualGuiItems::TICK)
                .alignedRight()
                .displayItems { _, output ->
                    output.acceptAll(CasualGuiItems.all())
                }
                .build()
        )
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(
            casual("heads"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 7)
                .title(Component.literal("Head Items"))
                .icon(CasualItems.GOLDEN_HEAD::getDefaultInstance)
                .alignedRight()
                .displayItems { _, output ->
                    output.accept(CasualItems.GOLDEN_HEAD)
                    output.accept(CasualItems.PLAYER_HEAD)
                    output.accept(CasualItems.FORWARD_FACING_PLAYER_HEAD)
                }
                .build()
        )
    }
}