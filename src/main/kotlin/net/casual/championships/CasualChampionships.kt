package net.casual.championships

import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.minigame.data.MinigameDataModule.Provider.Companion.register
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.casual.championships.commands.*
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.config.CasualConfig
import net.casual.championships.minigame.CasualMinigameManager
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.minigame.duel.CasualDuelArenas
import net.casual.championships.minigame.lobby.CasualLobbyData
import net.casual.championships.minigame.lobby.CasualLobbyParkourData
import net.casual.championships.minigame.lobby.LobbyStats
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.sync.CasualDatabaseSyncService
import net.casual.championships.sync.CasualNoopSyncService
import net.casual.championships.sync.CasualSyncService
import net.casual.championships.uhc.minigame.UHCMinigameFactory
import net.casual.championships.util.CasualRegistration
import net.casual.database.CasualDatabase
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.server.MinecraftServer

object CasualChampionships: DedicatedServerModInitializer {
    private const val MOD_ID = "casual-championships"

    private val container: ModContainer = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    var config = CasualConfig.read()
        private set

    var sync: CasualSyncService = CasualNoopSyncService
        private set

    val minigames = CasualMinigameManager(this::sync, CasualUtils.resolve("event_v2"))

    override fun onInitializeServer() {
        CasualUtils.logger.info("Starting CasualChampionships... Version: ${container.metadata.version}")

        CasualRegistration.register()

        CasualResourcePackHost.registerEvents()
        // CasualMinigames.registerEvents()
        CasualDuelArenas.registerEvents()
        LobbyStats.load()


        // TODO: We should register these somewhere else...
        UHCMinigameFactory.register(MinigameRegistries.MINIGAME_FACTORY)

        CasualLobbyData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)
        CasualLobbyParkourData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)

        this.minigames.registerEvents(GlobalEventHandler.Server)
        GlobalEventHandler.Server.register<ServerStartEvent>(priority = 0, listener = ::onServerStart)
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent>(::onServerRegisterCommand)
    }

    fun reload(server: MinecraftServer) {
        this.config = CasualConfig.read()

        CasualDuelArenas.reload(server)
        this.minigames.reload(server)

        this.reloadSyncService()
    }

    private fun onServerStart(event: ServerStartEvent) {
        this.minigames.load(event.server)
        this.reloadSyncService()
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