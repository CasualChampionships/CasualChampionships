package net.casual.championships.common

import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addAssetsFrom
import net.casual.arcade.resources.utils.ResourcePackUtils.addFont
import net.casual.arcade.resources.utils.ResourcePackUtils.addLangsFromData
import net.casual.arcade.resources.utils.ResourcePackUtils.addMissingItemModels
import net.casual.arcade.resources.utils.ResourcePackUtils.addSounds
import net.casual.arcade.scheduler.task.utils.TaskRegistries
import net.casual.championships.common.anticheat.CasualAntiCheat
import net.casual.championships.common.items.CasualGuiItems
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.task.GlowingBossbarTask
import net.casual.championships.common.task.GracePeriodBossbarTask
import net.casual.championships.common.util.*
import net.casual.championships.common.util.level.ReducedMobSpawningRules
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab

object CasualCommon: ModInitializer {
    const val MOD_ID = "casual-common"

    private val container = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    private val COMMON_PACK = NamedResourcePackCreator.named("common") {
        addAssetsFrom(container)
        addLangsFromData(MOD_ID, container)
        addFont(CasualComponents.Hud)
        addFont(CasualComponents.Gui)
        addFont(CasualComponents.Text)
        addFont(CasualComponents.Border)
        addSounds(CasualSounds)
        addMissingItemModels(CasualUtils.MOD_ID, container)
        packDescription = Component.literal("Common resources used in CasualChampionships")
    }

    val COMMON_PACKS = listOf(
        ArcadeResourcePacks.PADDING_FONT_PACK,
        ArcadeResourcePacks.PIXEL_FONT_PACK,
        ArcadeResourcePacks.HIDE_PLAYER_LIST_HEADS_PACK,
        ArcadeResourcePacks.MINI_MINECRAFT_FONT_PACK,
        ArcadeResourcePacks.SPACING_FONT_PACK,
        ArcadeResourcePacks.HIDE_PLAYER_LIST_PING_PACK,
        ArcadeResourcePacks.ACTION_BAR_FONT_PACK,
        ArcadeResourcePacks.MINI_ACTION_BAR_FONT_PACK,
        ArcadeResourcePacks.ARCADE_LANG_PACK,
        COMMON_PACK
    )

    override fun onInitialize() {
        CasualItems.load()
        CasualSounds.load()
        CasualStats.load()

        CasualAntiCheat.registerEvents()

        this.registerSpawningRules()
        this.registerItemGroups()

        // FIXME MOVE
        Registry.register(TaskRegistries.TASK_FACTORY, GracePeriodBossbarTask.id, GracePeriodBossbarTask)
        Registry.register(TaskRegistries.TASK_FACTORY, GlowingBossbarTask.id, GlowingBossbarTask)
    }

    @Deprecated("use casual() instead")
    fun id(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }

    private fun registerSpawningRules() {
        Registry.register(
            DimensionRegistries.CUSTOM_MOB_SPAWNING_RULES, casual("reduced_mob_cap"), ReducedMobSpawningRules
        )
    }

    private fun registerItemGroups() {
        PolymerItemGroupUtils.registerPolymerItemGroup(
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
        PolymerItemGroupUtils.registerPolymerItemGroup(
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