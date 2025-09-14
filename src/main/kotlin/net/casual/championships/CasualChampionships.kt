package net.casual.championships

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.minigame.data.MinigameDataModule.Provider.Companion.register
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.events.CasualConfigReloadedEvent
import net.casual.championships.minigame.CasualMinigames
import net.casual.championships.minigame.duel.CasualDuelArenas
import net.casual.championships.minigame.lobby.CasualLobbyData
import net.casual.championships.minigame.lobby.CasualLobbyParkourData
import net.casual.championships.minigame.lobby.LobbyStats
import net.casual.championships.resources.CasualResourcePackHost
import net.casual.championships.util.CasualConfig
import net.casual.championships.util.CasualRegistration
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.server.MinecraftServer
import org.slf4j.Logger

object CasualChampionships: DedicatedServerModInitializer {
    private const val MOD_ID = "casual-championships"

    private val container: ModContainer = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    @Deprecated("Use arcade utils logger", ReplaceWith("CasualUtils.logger", "net.casual.championships.common.util.CasualUtils"))
    val logger: Logger get() = CasualUtils.logger

    var config = CasualConfig.read()
        private set

    override fun onInitializeServer() {
        CasualUtils.logger.info("Starting CasualChampionships... Version: ${container.metadata.version}")

        CasualRegistration.register()

        CasualResourcePackHost.registerEvents()
        CasualMinigames.registerEvents()
        CasualDuelArenas.registerEvents()

        LobbyStats.load()

        CasualLobbyData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)
        CasualLobbyParkourData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)
    }

    // FIXME: Rework this
    fun reload(server: MinecraftServer) {
        this.config = CasualConfig.read()
        GlobalEventHandler.Server.broadcast(CasualConfigReloadedEvent(server, this.config))
    }
}