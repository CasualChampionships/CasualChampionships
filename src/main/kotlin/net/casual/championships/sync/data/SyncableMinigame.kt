package net.casual.championships.sync.data

import net.minecraft.resources.ResourceLocation
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class SyncableMinigame(
    val uuid: UUID,
    val type: ResourceLocation,
    val start: Instant,
    val end: Instant,
    val players: List<SyncablePlayer>
)
