package net.casual.championships.database

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.database.DatabaseWriter
import net.casual.arcade.minigame.serialization.MinigameDataTracker
import net.casual.arcade.utils.JsonUtils.writeForDatabase

class CasualDatabase {
    private val event = JsonObject()
    private val minigames = JsonArray()

    init {
        this.event.add("minigames", this.minigames)
    }

    fun writeStart() {
        this.event.addProperty("event_start_ms", System.currentTimeMillis())
    }

    fun writeEnd() {
        this.event.addProperty("event_end_ms", System.currentTimeMillis())
    }

    fun writeMinigame(minigame: MinigameDataTracker) {
        this.minigames.add(minigame.toJson())
    }

    fun write(writer: DatabaseWriter<*>) {
        this.toJson().writeForDatabase(writer)
    }

    fun toJson(): JsonObject {
        return this.event.deepCopy()
    }
}