package net.casualuhc.uhc.database

import com.google.gson.JsonObject
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import java.util.concurrent.CompletableFuture

interface UHCDataBase {
    fun clearLastStats()

    fun updateStats(player: ServerPlayer)

    fun combineStats()

    fun downloadTeams(): CompletableFuture<List<JsonObject>>

    fun incrementTeamWin(team: Team)

    fun shutdown()
}