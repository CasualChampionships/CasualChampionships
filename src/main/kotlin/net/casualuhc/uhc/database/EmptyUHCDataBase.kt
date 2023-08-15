package net.casualuhc.uhc.database

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import net.casualuhc.uhc.managers.TeamManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

class EmptyUHCDataBase: UHCDataBase {
    private val logger = LoggerFactory.getLogger("EmptyUHCDataBase")

    override fun clearLastStats() {
        this.logger.info("Clearing last stats")
    }

    override fun updateStats(player: ServerPlayer) {
        this.logger.info("Updating ${player.scoreboardName}'s stats")
    }

    override fun combineStats() {
        this.logger.info("Combining stats")
    }

    override fun downloadTeams(): CompletableFuture<List<JsonObject>> {
        this.logger.info("Retrieving local teams")
        return CompletableFuture.supplyAsync {
            val teams = ArrayList<JsonObject>()
            val path = TeamManager.configPath
            if (Files.exists(path)) {
                try {
                    Files.newBufferedReader(path).use {
                        val type = object: TypeToken<List<JsonObject>>() { }
                        teams.addAll(Gson().fromJson(it, type))
                    }
                } catch (e: Exception) {
                    this.logger.error("Failed to read $path", e)
                }
            }
            teams
        }
    }

    override fun incrementTeamWin(team: Team) {
        this.logger.info("Incrementing ${team.name}'s win")
    }

    override fun shutdown() {
        this.logger.info("Shutting down")
    }
}