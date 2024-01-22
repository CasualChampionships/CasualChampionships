package net.casual.championships.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils.any
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.JsonUtils.uuidOrNull
import net.casual.arcade.utils.json.JsonSerializer

object EventSerializer: JsonSerializer<Event> {
    override fun deserialize(json: JsonElement): Event {
        json as JsonObject
        val minigame = json.uuidOrNull("minigame")
        val teamSize = json.int("team_size")
        val operators = json.arrayOrDefault("operators").strings().toList()
        val lobby = json.any("lobby", LobbySerializer)
        val resourceData = json.obj("resources")
        val resources = EventResources(
            resourceData.string("host_ip"),
            resourceData.int("host_port"),
            resourceData.array("packs").strings().toList()
        )
        return Event(minigame, teamSize, operators, lobby, resources)
    }

    override fun serialize(value: Event): JsonElement {
        throw UnsupportedOperationException()
    }
}