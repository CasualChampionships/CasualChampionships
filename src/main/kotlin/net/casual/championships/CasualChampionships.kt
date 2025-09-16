package net.casual.championships

import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.minigame.data.MinigameDataModule.Provider.Companion.register
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ServerUtils.setMessageOfTheDay
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.convertCasing
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.casual.arcade.utils.string.SmallCapsTitleCase
import net.casual.arcade.utils.string.TitleCase
import net.casual.arcade.utils.toSmallCaps
import net.casual.championships.commands.*
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.config.CasualConfig
import net.casual.championships.lobby.minigame.modules.LobbyData
import net.casual.championships.lobby.minigame.modules.LobbyParkourData
import net.casual.championships.minigame.CasualMinigameManager
import net.casual.championships.minigame.duel.DuelArenas
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.sync.CasualDatabaseSyncService
import net.casual.championships.sync.CasualNoopSyncService
import net.casual.championships.sync.CasualSyncService
import net.casual.championships.uhc.minigame.UHCMinigameFactory
import net.casual.championships.util.CasualComponentUtils
import net.casual.database.CasualDatabase
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer

object CasualChampionships: DedicatedServerModInitializer {
    private const val MOD_ID = "casual-championships"

    private val container: ModContainer = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    var config = CasualConfig.read()
        private set

    var sync: CasualSyncService = CasualNoopSyncService
        private set

    val minigames = CasualMinigameManager(this, CasualUtils.resolve("event_v2"))

    override fun onInitializeServer() {
        CasualUtils.logger.info("Starting CasualChampionships... Version: ${container.metadata.version}")

        CasualResourcePackHost.registerEvents()
        DuelArenas.registerEvents()

        this.minigames.registerEvents(GlobalEventHandler.Server)
        GlobalEventHandler.Server.register<ServerStartEvent>(priority = 10_000, listener = ::onServerStart)
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent>(::onServerRegisterCommand)
    }

    fun reload(server: MinecraftServer) {
        this.config = CasualConfig.read()

        DuelArenas.reload(server)
        this.minigames.reload(server)

        this.reloadSyncService()
    }

    private fun onServerStart(event: ServerStartEvent) {
        val (server) = event
        this.minigames.load(server)
        this.reloadSyncService()

        server.setMessageOfTheDay(CasualComponentUtils.getMessageOfTheDay())
    }

    private fun onServerRegisterCommand(event: ServerRegisterCommandEvent) {
        event.register(CasualCommand, ViewCommand, ReplayCommand, RenameCommand, MinesweeperCommand)
    }

    private fun reloadSyncService() {
        val login = this.config.database
        if (login.url.isNotEmpty()) {
            val location = if (this.config.dev) "${login.name}_debug" else login.name
            val database = CasualDatabase(login.url + "/$location", login.username, login.password)
            this.sync = CasualDatabaseSyncService.create(database, this.minigames.event)
            return
        }

        ArcadeUtils.logger.info("No sync service provided, defaulting to noop")
        this.sync = CasualNoopSyncService
    }
}