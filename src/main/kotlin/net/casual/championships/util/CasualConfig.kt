package net.casual.championships.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import net.casual.championships.CasualChampionships
import net.casual.championships.common.util.CasualUtils
import org.apache.commons.lang3.SerializationException
import java.io.IOException
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@Serializable
data class CasualConfig(
    val dev: Boolean = false,
    @SerialName("database_login")
    val database: DatabaseLogin = DatabaseLogin()
) {
    @OptIn(ExperimentalSerializationApi::class)
    companion object {
        private val config = CasualUtils.resolve("config.json")

        private val json = Json {
            encodeDefaults = true
            prettyPrint = true
            prettyPrintIndent = "  "
            ignoreUnknownKeys = true
        }

        fun read(): CasualConfig {
            if (!this.config.exists()) {
                CasualChampionships.logger.info("Generating default config")
                return CasualConfig().also { this.write(it) }
            }
            return try {
                this.config.inputStream().use {
                    json.decodeFromStream(it)
                }
            } catch (e: Exception) {
                CasualChampionships.logger.error("Failed to read casual config, generating default", e)
                CasualConfig().also { this.write(it) }
            }
        }

        @JvmStatic
        fun write(config: CasualConfig) {
            try {
                this.config.parent.createDirectories()
                this.config.outputStream().use {
                    json.encodeToStream(config, it)
                }
            } catch (e: IOException) {
                CasualChampionships.logger.error("Failed to write casual config", e)
            } catch (e: SerializationException) {
                CasualChampionships.logger.error("Failed to serialize casual config", e)
            }
        }
    }
}
