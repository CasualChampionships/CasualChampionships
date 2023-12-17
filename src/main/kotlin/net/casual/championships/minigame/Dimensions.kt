package net.casual.championships.minigame

import net.casual.arcade.Arcade
import net.casual.championships.util.CasualUtils
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.DimensionTypes
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.chunk.ChunkGenerators
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.DimensionType
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.fantasy.util.VoidChunkGenerator

// TODO: Use fantasy
object Dimensions {
    private val type = ResourceKey.create(Registries.DIMENSION_TYPE, CasualUtils.id("default"))

    private val lobby = ResourceKey.create(Registries.DIMENSION, CasualUtils.id("lobby"))

    fun noop() {

    }

    fun getLobbyLevel(/*server: MinecraftServer*/): ServerLevel {
        // Fantasy.get(server).openTemporaryWorld(
        //     RuntimeWorldConfig()
        //         .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
        //         .setGenerator(VoidChunkGenerator(server.registryAccess().registryOrThrow(Registries.BIOME)))
        // )
        return Arcade.getServer().getLevel(lobby)!!
    }
}