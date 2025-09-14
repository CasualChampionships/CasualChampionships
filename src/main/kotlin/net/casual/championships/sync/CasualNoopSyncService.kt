package net.casual.championships.sync

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import net.casual.championships.sync.data.SyncableMinigame
import net.casual.championships.sync.data.SyncableParticipants
import net.casual.championships.sync.data.SyncableTeam

object CasualNoopSyncService: CasualSyncService {
    override fun getParticipants(): Deferred<SyncableParticipants> {
        return CompletableDeferred(SyncableParticipants.EMPTY)
    }

    override fun getTeams(): Deferred<List<SyncableTeam>> {
        return CompletableDeferred(listOf())
    }

    override fun syncUHC(minigame: SyncableMinigame): Deferred<Boolean> {
        return CompletableDeferred(false)
    }

    override fun syncDuel(minigame: SyncableMinigame): Deferred<Boolean> {
        return CompletableDeferred(false)
    }

    override fun close() {

    }
}