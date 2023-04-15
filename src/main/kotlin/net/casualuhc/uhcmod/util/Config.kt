package net.casualuhc.uhcmod.util

import com.google.gson.*
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerLoadedEvent
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.uhcmod.events.uhc.UHCConfigLoadedEvent
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object Config {
    private val root = FabricLoader.getInstance().configDir.resolve("CasualUHC")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create()

    private val configs = LinkedHashMap<String, Any?>()

    init {
        this.root.createDirectories()
    }

    fun resolve(path: String): Path {
        return this.root.resolve(path)
    }

    fun reload() {
        this.configs.clear()

        val path = this.resolve("config.json")
        if (path.exists()) {
            val json = path.bufferedReader().use { reader ->
                this.gson.fromJson(reader, JsonObject::class.java)
            }
            for ((key, value) in json.entrySet()) {
                this.configs[key] = this.parse(value)
            }
        }

        EventHandler.broadcast(UHCConfigLoadedEvent())
    }

    fun write() {
        this.resolve("config.json").bufferedWriter().use { writer ->
            this.gson.toJson(this.configs, writer)
        }
    }

    fun string(key: String): String {
        return this.getAs(key)
    }

    fun stringOrNull(key: String): String? {
        return this.getAsOrNull(key)
    }

    fun stringOrDefault(key: String, default: String = ""): String {
        return this.getAsOrDefault(key, default)
    }

    fun boolean(key: String): Boolean {
        return this.getAs(key)
    }

    fun booleanOrNull(key: String): Boolean? {
        return this.getAsOrNull(key)
    }

    fun booleanOrDefault(key: String, default: Boolean = false): Boolean {
        return this.getAsOrDefault(key, default)
    }

    fun number(key: String): Number {
        return this.getAs(key)
    }

    fun numberOrNull(key: String): Number? {
        return this.getAsOrNull(key)
    }

    fun numberOrDefault(key: String, default: Number): Number {
        return this.getAsOrDefault(key, default)
    }

    fun int(key: String): Int {
        return this.number(key).toInt()
    }

    fun intOrNull(key: String): Int? {
        return this.numberOrNull(key)?.toInt()
    }

    fun intOrDefault(key: String, default: Int = 0): Int {
        return this.numberOrDefault(key, default).toInt()
    }

    fun double(key: String): Double {
        return this.number(key).toDouble()
    }

    fun doubleOrNull(key: String): Double? {
        return this.numberOrNull(key)?.toDouble()
    }

    fun doubleOrDefault(key: String, default: Double = 0.0): Double {
        return this.numberOrDefault(key, default).toDouble()
    }

    fun <T> list(key: String): List<T> {
        return this.getAs(key)
    }

    fun <T> listOrNull(key: String): List<T>? {
        return this.getAsOrNull(key)
    }

    fun <T> listOrDefault(key: String, default: List<T> = emptyList()): List<T> {
        return this.getAsOrDefault(key, default)
    }

    internal fun registerEvents() {
        EventHandler.register<ServerLoadedEvent>(0) { reload() }
        EventHandler.register<ServerStoppedEvent> { this.write() }
    }

    private inline fun <reified T: Any> getAs(key: String): T {
        if (!this.configs.containsKey(key)) {
            throw IllegalArgumentException("Failed to get $key from config, it doesn't exist")
        }
        val value = this.getAsOrNull<T>(key)
        if (value === null) {
            throw IllegalArgumentException("Failed to get $key from config, it was null")
        }
        return value
    }

    private inline fun <reified T: Any> getAsOrNull(key: String): T? {
        val value = this.configs[key]
        if (value !== null && value !is T) {
            val message = "Failed to get $key from config as type ${T::class.java}, found ${value::class.java} instead"
            throw IllegalArgumentException(message)
        }
        return value as? T
    }

    private inline fun <reified T: Any> getAsOrDefault(key: String, default: T): T {
        val value = this.configs[key]
        if (value is T) {
            return value
        }
        if (value !== null) {
            val message = "Failed to get $key from config as type ${T::class.java}, found ${value::class.java} instead"
            throw IllegalArgumentException(message)
        }
        this.configs[key] = default
        return default
    }

    private fun parse(element: JsonElement): Any? {
        if (element is JsonObject) {
            val map = LinkedHashMap<String, Any?>()
            for ((key, value) in element.entrySet()) {
                map[key] = this.parse(value)
            }
            return map
        }
        if (element is JsonArray) {
            val list = ArrayList<Any?>()
            for (value in element.asJsonArray) {
                list.add(this.parse(value))
            }
            return list
        }
        if (element is JsonPrimitive) {
            return when {
                element.isBoolean -> element.asBoolean
                element.isNumber -> element.asNumber
                else -> element.asString
            }
        }
        return null
    }
}
