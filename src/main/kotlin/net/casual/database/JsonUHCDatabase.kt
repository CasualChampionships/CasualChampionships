package net.casual.database

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.utils.JsonUtils.double
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.toJsonArray
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.extensions.PlayerFlag
import net.casual.extensions.PlayerFlagsExtension.Companion.flags
import net.casual.extensions.PlayerStat
import net.casual.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casual.minigame.uhc.UHCMinigame
import net.casual.util.Config
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import org.apache.commons.lang3.StringUtils
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

class JsonUHCDatabase: UHCDataBase {
    private val totalStats = HashMap<UUID, JsonObject>()
    private val latestStats = HashMap<UUID, JsonObject>()

    init {
        if (path.exists()) {
            val jsons = path.bufferedReader().use {
                gson.fromJson(it, JsonObject::class.java)
            }
            val latest = jsons["latest"].asJsonArray.objects().associateBy { UUID.fromString(it.string("_id")) }
            val total = jsons["total"].asJsonArray.objects().associateBy { UUID.fromString(it.string("_id")) }
            this.totalStats.putAll(total)
            this.latestStats.putAll(latest)
        }
    }

    override fun clearLastStats() {
        this.latestStats.clear()
    }

    override fun updateStats(player: ServerPlayer) {
        val stats = player.uhcStats
        val latest = JsonObject()
        latest["_id"] = player.stringUUID
        latest["participated"] = player.flags.has(PlayerFlag.Participating)
        latest["won"] = player.flags.has(PlayerFlag.Won)
        for (stat in PlayerStat.values()) {
            latest[stat.id()] = stats[stat]
        }
        val advancements = stats.advancements().map { a ->
            val json = JsonObject()
            json["id"] = a.id.toString()
            val display = a.display
            if (display == null) {
                json["item"] = "stone"
                json["title"] = a.id.path.split('_').joinToString(" ") { StringUtils.capitalize(it) }
            } else {
                json["item"] = display.icon.item.toString()
                json["title"] = display.title.string
            }
            json
        }.collect(::JsonArray, JsonArray::add, JsonArray::addAll)
        latest["advancements"] = advancements
        this.latestStats[player.uuid] = latest
    }

    override fun combineStats() {
        for ((uuid, stats) in this.latestStats) {
            val latest = stats.deepCopy()
            val json = this.totalStats[uuid]
            val participated = latest.remove("participated").asBoolean
            val won = latest.remove("won").asBoolean

            if (json == null) {
                latest["plays"] = if (participated) 1 else 0
                latest["wins"] = if (won) 1 else 0
                this.totalStats[uuid] = latest
                continue
            }

            json["plays"] = json.int("plays") + if (participated) 1 else 0
            json["wins"] = json.int("wins") + if (won) 1 else 0
            for (stat in PlayerStat.values()) {
                val id = stat.id()
                json[id] = stat.merger(json.double(id), latest.double(id))
            }

            this.totalStats[uuid] = json
        }
    }

    override fun downloadTeams(): CompletableFuture<List<JsonObject>> {
        return EmptyUHCDataBase.downloadTeams()
    }

    override fun incrementTeamWin(team: Team) {

    }

    override fun shutdown() {
        val json = JsonObject()
        json["latest"] = this.latestStats.values.toJsonArray { it }
        json["total"] = this.totalStats.values.toJsonArray { it }

        path.bufferedWriter().use {
            gson.toJson(json, it)
        }
    }

    companion object {
        private val path = Config.resolve("stats.json")

        private val gson = GsonBuilder()
            .disableHtmlEscaping()
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .setPrettyPrinting()
            .create()
    }
}