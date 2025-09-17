package net.casual.championships.common.util

import net.casual.arcade.utils.ResourceLocation
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

fun casual(path: String): ResourceLocation {
    return ResourceLocation(CasualUtils.MOD_ID, path)
}

object CasualUtils {
    private val root = FabricLoader.getInstance().configDir.resolve("casual-championships")

    const val MOD_ID = "casual"

    val logger: Logger = LoggerFactory.getLogger("CasualChampionships")

    fun resolve(next: String): Path {
        return this.root.resolve(next)
    }
}