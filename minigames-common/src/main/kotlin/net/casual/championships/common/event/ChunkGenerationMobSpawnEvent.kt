package net.casual.championships.common.event

import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.threading.AsyncEvent
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.ServerLevelAccessor
import net.minecraft.world.level.biome.MobSpawnSettings

data class ChunkGenerationMobSpawnEvent(
    val level: ServerLevelAccessor,
    val chunkPos: ChunkPos,
    val settings: MobSpawnSettings,
    var probability: Float
): ServerSideEvent, AsyncEvent