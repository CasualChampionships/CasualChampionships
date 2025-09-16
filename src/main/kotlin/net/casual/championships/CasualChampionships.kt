package net.casual.championships

import net.casual.arcade.commands.register
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerRegisterCommandEvent
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.minigame.data.MinigameDataModule.Provider.Companion.register
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ArcadeUtils
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
import net.casual.championships.minigame.duel.CasualDuelArenas
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

        // CasualRegistration.register()

        CasualResourcePackHost.registerEvents()
        // CasualMinigames.registerEvents()
        CasualDuelArenas.registerEvents()
        // LobbyStats.load()


        // TODO: We should register these somewhere else...
        UHCMinigameFactory.register(MinigameRegistries.MINIGAME_FACTORY)
        LobbyData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)
        LobbyParkourData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)

        this.minigames.registerEvents(GlobalEventHandler.Server)
        GlobalEventHandler.Server.register<ServerStartEvent>(priority = 10_000, listener = ::onServerStart)
        GlobalEventHandler.Server.register<ServerRegisterCommandEvent>(::onServerRegisterCommand)
    }

    fun reload(server: MinecraftServer) {
        this.config = CasualConfig.read()

        CasualDuelArenas.reload(server)
        this.minigames.reload(server)

        this.reloadSyncService()
    }

    fun getMessageOfTheDay(): Component {
        return Component {
            val cc = literal("Casual Championships".convertCasing(TitleCase, SmallCapsTitleCase)).bold().color(0xFFAC1C)
            val title = literal("\uD83D\uDDE1").yellow() + " " + cc + " " + literal("\uD83C\uDFF9").yellow()
            val subtitle = literal("   be prepared".toSmallCaps()).lime() + wrap() +
                literal(" ◆ ").white() + literal("let the chaos ensue    ".toSmallCaps()).lime()
            empty() + literal("╔═════", 0x009BFF, 0x80CDFF) + title + literal("═════╗", 0x80CDFF, 0x009BFF) + nl +
                literal("╚══", 0x009BFF, 0x33AFFF) + subtitle + literal("══╝", 0x33AFFF, 0x009BFF)
        }
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