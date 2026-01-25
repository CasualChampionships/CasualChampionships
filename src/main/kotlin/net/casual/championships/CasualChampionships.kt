package net.casual.championships

import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.utils.ServerUtils.setMessageOfTheDay
import net.casual.arcade.utils.coroutine.launch
import net.casual.championships.commands.*
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.config.CasualConfig
import net.casual.championships.config.DatabaseLogin
import net.casual.championships.minigame.CasualMinigameManager
import net.casual.championships.minigame.duel.DuelArenas
import net.casual.championships.minigame.duel.DuelKits
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.sync.CasualDatabaseSyncService
import net.casual.championships.sync.CasualNoopSyncService
import net.casual.championships.sync.CasualSyncService
import net.casual.championships.util.CasualComponentUtils
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

    val minigames = CasualMinigameManager(this, CasualUtils.resolve("event"))

    override fun onInitializeServer() {
        CasualUtils.logger.info("Starting CasualChampionships... Version: ${container.metadata.version}")

        CasualResourcePackHost.registerEvents()
        DuelArenas.registerEvents()
        DuelKits.registerEvents()

        this.minigames.registerEvents(GlobalEventHandler.Server)
        GlobalEventHandler.Server.register<ServerStartEvent>(priority = 10_000, listener = ::onServerStart)
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent>(::onServerRegisterCommand)
    }

    fun reload(server: MinecraftServer) {
        this.config = CasualConfig.read()

        DuelArenas.reload(server)
        DuelKits.reload(server)
        this.minigames.reload(server)

        this.reloadSyncService(server)
    }

    private fun onServerStart(event: ServerStartEvent) {
        val (server) = event
        this.minigames.load(server)
        this.reloadSyncService(server)

        server.setMessageOfTheDay(CasualComponentUtils.getMessageOfTheDay())
    }

    private fun onServerRegisterCommand(event: ServerRegisterCommandEvent) {
        event.register(CasualCommand, ViewCommand, ReplayCommand, RenameCommand, MinesweeperCommand)
    }

    private fun reloadSyncService(server: MinecraftServer) {
        val login = this.config.database
        if (login.url.isEmpty()) {
            CasualUtils.logger.info("No sync service provided, defaulting to noop")
            this.sync = CasualNoopSyncService
            return
        }

        val location = if (this.config.dev) "${login.name}_debug" else login.name
        server.launch {
            loadDatabaseSync(login.copy(url = "${login.url}/$location"), minigames.event)
            minigames.createTeams()
        }
    }

    private suspend fun loadDatabaseSync(login: DatabaseLogin, event: String) {
        try {
            sync = CasualDatabaseSyncService.create(login, event)
        } catch (exception: Exception) {
            CasualUtils.logger.error("Failed to load database sync, falling back to noop", exception)
            sync = CasualNoopSyncService
        }
    }
}