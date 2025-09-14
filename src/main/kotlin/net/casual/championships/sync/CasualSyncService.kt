@file:OptIn(ExperimentalTime::class)

package net.casual.championships.sync

import com.mojang.authlib.GameProfile
import kotlinx.coroutines.Deferred
import net.casual.arcade.minigame.Minigame
import net.casual.championships.duel.minigame.DuelMinigame
import net.casual.championships.sync.data.SyncableMinigame
import net.casual.championships.sync.data.SyncableParticipants
import net.casual.championships.sync.data.SyncablePlayer
import net.casual.championships.sync.data.SyncableTeam
import net.casual.championships.uhc.minigame.UHCMinigame
import kotlin.time.ExperimentalTime

fun CasualSyncService.syncMinigame(minigame: Minigame): Deferred<Boolean> {
    val syncer = when (minigame) {
        is UHCMinigame -> this::syncUHC
        is DuelMinigame -> this::syncDuel
        else -> throw IllegalArgumentException("Cannot sync minigame type ${minigame.id}")
    }

    val scoreboard = minigame.server.scoreboard
    val players = minigame.players.allProfiles.mapNotNull(fun(profile): SyncablePlayer? {
        val team = scoreboard.getPlayersTeam(profile.name) ?: return null
        val tracker = minigame.stats.getOrCreateTracker(profile.id)
        val advancements = minigame.data.getAdvancements(profile.id)
        return SyncablePlayer(profile, SyncableTeam.from(team), tracker, advancements)
    })
    val syncable = SyncableMinigame(
        minigame.uuid, minigame.id, minigame.data.startTime, minigame.data.endTime, players
    )
    return syncer.invoke(syncable)
}

interface CasualSyncService: AutoCloseable {
    fun getParticipants(): Deferred<SyncableParticipants>

    fun getTeams(): Deferred<List<SyncableTeam>>

    fun syncUHC(minigame: SyncableMinigame): Deferred<Boolean>

    fun syncDuel(minigame: SyncableMinigame): Deferred<Boolean>
}