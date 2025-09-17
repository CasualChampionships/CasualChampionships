package net.casual.championships.sync

import net.casual.championships.sync.data.SyncableMinigame
import net.casual.championships.sync.data.SyncableParticipants
import net.casual.championships.sync.data.SyncableTeam

object CasualNoopSyncService: CasualSyncService {
    override suspend fun getParticipants(): SyncableParticipants {
        return SyncableParticipants.EMPTY
    }

    override suspend fun getTeams(): List<SyncableTeam> {
        return listOf()
    }

    override suspend fun syncUHC(minigame: SyncableMinigame): Boolean {
        return false
    }

    override suspend fun syncDuel(minigame: SyncableMinigame): Boolean {
        return false
    }

    override fun close() {

    }
}