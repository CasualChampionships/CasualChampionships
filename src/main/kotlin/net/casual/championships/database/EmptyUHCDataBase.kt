package net.casual.championships.database

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import net.casual.championships.managers.TeamManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.concurrent.CompletableFuture

object EmptyUHCDataBase: UHCDataBase {
    private val logger = LoggerFactory.getLogger("EmptyUHCDataBase")

    override fun clearLastStats() {
        logger.info("Clearing last stats")
    }

    override fun updateStats(player: ServerPlayer) {
        logger.info("Updating ${player.scoreboardName}'s stats")
    }

    override fun combineStats() {
        logger.info("Combining stats")
    }

    override fun downloadTeams(): CompletableFuture<List<JsonObject>> {
        logger.info("Retrieving local teams")
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
                    logger.error("Failed to read $path", e)
                }
            }
            teams
        }
    }

    override fun incrementTeamWin(team: Team) {
        logger.info("Incrementing ${team.name}'s win")
    }

    override fun shutdown() {
        logger.info("Shutting down")
    }
}