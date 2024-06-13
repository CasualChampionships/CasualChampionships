package net.casual.championships.data

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.JsonUtils
import net.casual.championships.CasualMod
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.uhc.UHCMinigame
import net.casual.championships.util.CasualConfig
import net.minecraft.server.MinecraftServer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

class JsonDataManager: DataManager {
    override fun createTeams(server: MinecraftServer) {

    }

    override fun syncUHCData(uhc: UHCMinigame) {
        this.syncMinigameData(uhc)
    }

    override fun syncDuelData(duel: DuelMinigame) {
        this.syncMinigameData(duel)
    }

    override fun close() {

    }

    private fun syncMinigameData(minigame: Minigame<*>) {
        val serialized = minigame.data.toJson()
        CompletableFuture.runAsync {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                val currentDate = format.format(Date())
                var path = stats.resolve("${minigame.id} $currentDate.json")
                if (path.exists()) {
                    path = stats.resolve("${minigame.id} (${minigame.uuid}) $currentDate.json")
                }
                path.bufferedWriter().use {
                    JsonUtils.encode(serialized, it)
                }
            } catch (e: Exception) {
                CasualMod.logger.error("Failed to write stats!", e)
                // So we have it somewhere!
                CasualMod.logger.error(JsonUtils.GSON.toJson(serialized))
            }
        }
    }

    private companion object {
        val stats = CasualConfig.resolve("stats")
    }
}