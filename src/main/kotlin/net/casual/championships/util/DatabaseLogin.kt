package net.casual.championships.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.json.JsonSerializer

data class DatabaseLogin(
    val url: String = "",
    val username: String = "",
    val password: String = ""
)

object DatabaseLoginSerializer: JsonSerializer<DatabaseLogin> {
    override fun serialize(value: DatabaseLogin): JsonElement {
        val json = JsonObject()
        json.addProperty("url", value.url)
        json.addProperty("username", value.username)
        json.addProperty("password", value.password)
        return json
    }

    override fun deserialize(json: JsonElement): DatabaseLogin {
        json as JsonObject
        return DatabaseLogin(
            json.string("url"),
            json.string("username"),
            json.string("password")
        )
    }
}