package net.casual.championships.minigame

import com.google.gson.JsonObject
import net.casual.arcade.area.StructuredAreaConfig
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.minigame.MinigameCloseEvent
import net.casual.arcade.events.player.PlayerCanLoginEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.events.MinigamesEventConfig
import net.casual.arcade.minigame.events.MinigamesEventConfigSerializer
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.JsonUtils
import net.casual.championships.common.ui.LobbyBossBarConfig
import net.casual.championships.duel.DuelMinigame
import net.casual.championships.events.CasualConfigReloaded
import net.casual.championships.managers.TeamManager
import net.casual.championships.uhc.UHCMinigame
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
    val event: CasualChampionshipsEvent

    val minigame: Minigame<*>
        get() = this.event.current

    @JvmField
    var floodgates = false

    init {


        this.path = Config.resolve("event")
        this.config = MinigamesEventConfigSerializer().apply {
            val lobbies = Config.resolve("lobbies").createDirectories()
            addAreaFactory(StructuredAreaConfig.factory(lobbies))
            addBossbarFactory(LobbyBossBarConfig)
        }

        this.event = CasualChampionshipsEvent(this.readMinigameEvent())
    }

    internal fun registerEvents() {
        Minigames.registerFactory(UHCMinigame.ID, this.event::createUHCMinigame)
        Minigames.registerFactory(DuelMinigame.ID, this.event::createDuelMinigame)

        GlobalEventHandler.register<PlayerCanLoginEvent> { event ->
            if (!floodgates && !event.server.playerList.isOp(event.profile)) {
                event.cancel("CasualChampionships isn't quite ready yet...".literal())
                this.minigame.chat.broadcastTo(
                    "${event.profile.name} tried to join, but floodgates are closed".literal(),
                    this.minigame.getAdminPlayers()
                )
            }
        }

        GlobalEventHandler.register<ServerLoadedEvent>(Int.MAX_VALUE) {
            this.loadMinigameEventData(it.server)
        }

        GlobalEventHandler.register<PlayerJoinEvent> {
            val player = it.player
            this.event.addPlayer(player)
        }

        GlobalEventHandler.register<ServerSaveEvent> {
            this.writeMinigameEventData()
        }
        GlobalEventHandler.register<ServerStoppingEvent> {
            this.writeMinigameEvent(this.event.config)
        }

        GlobalEventHandler.register<CasualConfigReloaded> {
            this.event.config = this.readMinigameEvent()
        }
    }

    private fun readMinigameEvent(): MinigamesEventConfig {
        val path = this.path.resolve("event.json")
        if (path.exists()) {
            path.bufferedReader().use {
                val json = JsonUtils.GSON.fromJson(it, JsonObject::class.java)
                return this.config.deserialize(json)
            }
        }
        this.writeMinigameEvent(MinigamesEventConfig.DEFAULT)
        return MinigamesEventConfig.DEFAULT
    }

    private fun writeMinigameEvent(config: MinigamesEventConfig) {
        val path = this.path.resolve("event.json")
        path.parent.createDirectories()
        path.bufferedWriter().use {
            JsonUtils.GSON.toJson(this.config.serialize(config), it)
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
            TeamManager.createTeams()
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