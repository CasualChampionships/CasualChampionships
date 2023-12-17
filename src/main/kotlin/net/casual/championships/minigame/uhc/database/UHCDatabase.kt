package net.casual.championships.minigame.uhc.database

import com.google.gson.JsonObject
import net.casual.championships.minigame.uhc.UHCMinigame
import net.minecraft.world.scores.Team
import java.util.concurrent.CompletableFuture

open class UHCDatabase {
    open fun update(uhc: UHCMinigame) {

    }

    open fun downloadTeams(): CompletableFuture<List<JsonObject>> {
        return CompletableFuture.completedFuture(listOf())
    }

    open fun incrementTeamWin(team: Team) {

    }

    open fun shutdown() {

    }
}