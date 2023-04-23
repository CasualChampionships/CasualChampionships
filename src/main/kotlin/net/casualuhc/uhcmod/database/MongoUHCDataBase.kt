package net.casualuhc.uhcmod.database

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.model.Filters
import com.mongodb.client.model.ReplaceOptions
import net.casualuhc.uhcmod.extensions.PlayerFlag.Participating
import net.casualuhc.uhcmod.extensions.PlayerFlag.Won
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.PlayerStat
import net.casualuhc.uhcmod.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casualuhc.uhcmod.managers.TeamManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.Team
import org.apache.commons.lang3.StringUtils
import org.bson.BsonDocument
import org.bson.Document
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class MongoUHCDataBase(
    name: String,
    uri: String,
    last: String,
    combined: String,
    team: String
): UHCDataBase {
    private val logger = LoggerFactory.getLogger("MongoUHCDataBase")

    private val client = MongoClient(MongoClientURI(uri))
    private val database = this.client.getDatabase(name)
    private val lastPlayerStats = this.database.getCollection(last)
    private val combinedPlayerStats = this.database.getCollection(combined)
    private val teams = this.database.getCollection(team)
    private val executor = Executors.newSingleThreadExecutor()

    override fun clearLastStats() {
        this.executor.execute {
            val result = this.lastPlayerStats.deleteMany(BsonDocument())
            this.logger.info("Cleared ${result.deletedCount} player stats")
        }
    }

    override fun updateStats(player: ServerPlayer) {
        this.executor.execute {
            val stats = player.uhcStats
            val filter = Filters.eq("_id", player.stringUUID)
            val latest = Document("_id", player.stringUUID)
            latest["participated"] = player.flags.has(Participating)
            latest["won"] = player.flags.has(Won)
            for (stat in PlayerStat.values()) {
                latest[stat.id()] = stats[stat]
            }
            val advancements = stats.advancements().map { a ->
                val bson = BasicDBObject()
                bson["id"] = a.id.toString()
                val display = a.display
                if (display == null) {
                    bson["item"] = "stone"
                    bson["title"] = a.id.path.split('_').joinToString(" ") { StringUtils.capitalize(it) }
                } else {
                    bson["item"] = display.icon.item.toString()
                    bson["title"] = display.title.string
                }
                bson
            }.collect(::BasicDBList, BasicDBList::add, BasicDBList::addAll)
            latest["advancements"] = advancements
            this.lastPlayerStats.replaceOne(filter, latest, ReplaceOptions().upsert(true))
        }
    }

    override fun combineStats() {
        this.executor.execute {
            for (stats in this.lastPlayerStats.find()) {
                val filter = Filters.eq("_id", stats["_id"])
                val cursor = this.combinedPlayerStats.find(filter).cursor()
                val participated = stats.remove("participated") as Boolean
                val won = stats.remove("won") as Boolean

                if (!cursor.hasNext()) {
                    stats["plays"] = if (participated) 1 else 0
                    stats["wins"] = if (won) 1 else 0
                    this.combinedPlayerStats.insertOne(stats)
                    continue
                }

                val total = cursor.next()
                total["plays"] = total.getInteger("plays") + if (participated) 1 else 0
                total["wins"] = total.getInteger("wins") + if (won) 1 else 0
                for (stat in PlayerStat.values()) {
                    val id = stat.id()
                    val new = stat.merger(total.getDouble(id), stats.getDouble(id))
                    total.replace(id, new)
                }

                if (cursor.hasNext()) {
                    this.logger.warn("Database has multiple players with same uuid ${stats["_id"]}!!")
                }

                this.combinedPlayerStats.replaceOne(filter, total)
            }
        }
    }

    override fun downloadTeams(): CompletableFuture<List<JsonObject>> {
        return CompletableFuture.supplyAsync({
            val teams = ArrayList<JsonObject>()
            try {
                val gson = GsonBuilder().setPrettyPrinting().create()
                for (document in this.teams.find()) {
                    teams.add(gson.fromJson(document.toJson(), JsonObject::class.java))
                }
                Files.newBufferedWriter(TeamManager.configPath).use {
                    gson.toJson(teams, it)
                }
                this.logger.info("Successfully downloaded Teams.json")
            } catch (e: Exception) {
                this.logger.error("Could not download Teams.json", e)
            }
            teams
        }, this.executor)
    }

    override fun incrementTeamWin(team: Team) {
        this.executor.execute {
            val filter = Filters.eq("name", team.name)
            this.teams.find(filter).cursor().use { cursor ->
                if (cursor.hasNext()) {
                    val document: Document = cursor.next()
                    val currentWins = document.getInteger("wins")
                    document.replace("wins", currentWins + 1)
                    this.teams.replaceOne(filter, document)
                } else {
                    this.logger.error("Winning team cannot be found")
                }
            }
        }
    }

    override fun shutdown() {
        this.client.close()
        this.executor.shutdown()
    }
}