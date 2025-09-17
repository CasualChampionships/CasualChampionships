package net.casual.championships.sync.data

import com.mojang.authlib.GameProfile

sealed interface SyncableParticipants {
    val profiles: Set<GameProfile>

    class Strict(override val profiles: Set<GameProfile>): SyncableParticipants

    class Relaxed(override val profiles: Set<GameProfile>): SyncableParticipants

    companion object {
        val EMPTY: SyncableParticipants = Relaxed(setOf())
    }
}