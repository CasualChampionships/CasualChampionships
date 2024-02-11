package net.casual.championships.minigame

import com.google.gson.JsonObject
import net.casual.arcade.area.StructuredAreaConfig
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.MinigamesEventConfig
import net.casual.arcade.minigame.events.MinigamesEventConfigSerializer
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.championships.minigame.lobby.ui.LobbyBossBarConfig
import net.casual.championships.util.CasualUtils
import net.casual.championships.util.Config
import net.minecraft.server.MinecraftServer
import java.nio.file.Path
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@Suppress("JoinDeclarationAndAssignment")
object CasualMinigames {
    private val path: Path
    private val config: MinigamesEventConfigSerializer
    lateinit var event: CasualMinigamesEvent

    val minigame: Minigame<*>
        get() = this.event.current

    @JvmField
    var floodgates = false

    init {
        this.path = Config.resolve("event")
        this.config = MinigamesEventConfigSerializer().apply {
            addAreaFactory(StructuredAreaConfig.factory(Config.resolve("lobbies")))
            addBossbarFactory(LobbyBossBarConfig)
        }

        this.readMinigameEvent()
    }

    internal fun registerEvents() {
        Minigames.registerFactory(CasualUtils.id("uhc_minigame"), this.event::createUHCMinigame)
        Minigames.registerFactory(CasualUtils.id("duel_minigame"), this.event::createDuelMinigame)

        GlobalEventHandler.register<ServerLoadedEvent>(Int.MAX_VALUE) {
            this.loadMinigameEventData(it.server)
        }

        GlobalEventHandler.register<PlayerJoinEvent> {
            val player = it.player
            if (player.getMinigame() == null) {
                this.event.addPlayer(player)
            }
        }

        GlobalEventHandler.register<ServerSaveEvent> {
            this.writeMinigameEventData()
        }
        GlobalEventHandler.register<ServerStoppingEvent> {
            this.writeMinigameEvent()
        }
    }

    private fun readMinigameEvent() {
        val path = this.path.resolve("event.json")
        if (path.exists()) {
            path.bufferedReader().use {
                val json = JsonUtils.GSON.fromJson(it, JsonObject::class.java)
                this.event = CasualMinigamesEvent(this.config.deserialize(json))
            }
            return
        }
        this.event = CasualMinigamesEvent(MinigamesEventConfig.DEFAULT)
        this.writeMinigameEvent()
    }

    private fun writeMinigameEvent() {
        val path = this.path.resolve("event.json")
        path.parent.createDirectories()
        path.bufferedWriter().use {
            JsonUtils.GSON.toJson(this.config.serialize(this.event.config), it)
        }
    }

    private fun loadMinigameEventData(server: MinecraftServer) {
        val eventData = this.path.resolve("event_data.json")
        if (eventData.exists()) {
            eventData.bufferedReader().use {
                val json = JsonUtils.GSON.fromJson(it, JsonObject::class.java)
                this.event.deserialize(json, server)
            }
        } else {
            this.event.returnToLobby(server)
        }
    }

    private fun writeMinigameEventData() {
        val eventData = this.path.resolve("event_data.json")
        eventData.parent.createDirectories()
        eventData.bufferedWriter().use {
            JsonUtils.GSON.toJson(this.event.serialize(), it)
        }
    }
}