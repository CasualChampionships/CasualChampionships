package net.casual.championships.sync.data

import net.minecraft.resources.Identifier
import java.util.*
import kotlin.time.Instant

data class SyncableMinigame(
    val uuid: UUID,
    val type: Identifier,
    val start: Instant,
    val end: Instant,
    val players: List<SyncablePlayer>
)
