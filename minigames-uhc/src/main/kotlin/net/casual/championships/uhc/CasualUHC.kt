package net.casual.championships.uhc

import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.pack.generation.PackDefinition
import net.casual.arcade.scheduler.utils.TaskRegistries
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension
import net.casual.championships.uhc.gui.UHCMapRenderer
import net.casual.championships.uhc.item.UHCItems
import net.casual.championships.uhc.minigame.UHCMinigameFactory
import net.casual.championships.uhc.boundary.UHCBoundaryRoutine
import net.casual.championships.uhc.routine.GlowingCountdownRoutine
import net.casual.championships.uhc.routine.GraceCountdownRoutine
import net.casual.championships.uhc.utils.UHCComponents
import net.casual.championships.uhc.utils.UHCStats
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CasualUHC: ModInitializer {
    const val MOD_ID = "casual-uhc"

    internal val container = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    internal val logger: Logger = LoggerFactory.getLogger("CasualUHC")

    val UHC_PACK = PackDefinition("uhc") {
        description = Component.literal("Resources for CasualChampionships UHC minigame")
        include(MOD_ID)
        addLangs(MOD_ID)
        generateMissingItemModels(CasualUtils.MOD_ID, container)
        addFont(UHCComponents.Bitmap)
    }

    override fun onInitialize() {
        UHCMapRenderer.load()
        UHCItems.load()
        UHCStats.load()

        TeamSharedHealthExtension.registerEvents()

        UHCMinigameFactory.register(MinigameRegistries.MINIGAME_FACTORY)

        UHCBoundaryRoutine.register(TaskRegistries.ROUTINE)
        GlowingCountdownRoutine.register(TaskRegistries.ROUTINE)
        GraceCountdownRoutine.register(TaskRegistries.ROUTINE)
    }
}