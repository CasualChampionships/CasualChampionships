package net.casual.championships.database

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.model.Filters
import net.casual.arcade.utils.JsonUtils
import net.casual.championships.uhc.UHCMinigame
import net.minecraft.world.scores.Team
import org.bson.Document
import org.bson.codecs.DecoderContext
import org.bson.codecs.DocumentCodec
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

// TODO:
class MongoUHCDatabase(
    name: String,
    uri: String,
    uhc: String,
    team: String
): UHCDatabase() {
    private val logger = LoggerFactory.getLogger("MongoUHCDataBase")

    private val client = MongoClient(MongoClientURI(uri))
    private val database = this.client.getDatabase(name)
    private val uhc = this.database.getCollection(uhc)
    private val teams = this.database.getCollection(team)
    private val executor = Executors.newSingleThreadExecutor()

    override fun update(uhc: UHCMinigame) {
        // val bson = uhc.data.toJson().writeForDatabase(MongoDatabaseWriter()).element.asDocument()
        // bson["_id"] = bson.remove("uuid")
        // this.uhc.insertOne(
        //     DocumentCodec().decode(bson.asBsonReader(), DecoderContext.builder().build())
        // )
    }

    override fun downloadTeams(): CompletableFuture<List<JsonObject>> {
        return CompletableFuture.supplyAsync({
            val teams = ArrayList<JsonObject>()
            try {
                for (document in this.teams.find()) {
                    teams.add(JsonUtils.decodeToJsonObject(document.toJson().reader()))
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
        this.executor.shutdownNow().forEach(Runnable::run)
        this.client.close()
    }
}