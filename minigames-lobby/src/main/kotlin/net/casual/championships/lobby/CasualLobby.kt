package net.casual.championships.lobby

import net.casual.arcade.minigame.data.MinigameDataModule.Provider.Companion.register
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.championships.lobby.minigame.modules.LobbyData
import net.casual.championships.lobby.minigame.modules.LobbyParkourData
import net.casual.championships.lobby.stats.LobbyStats
import net.fabricmc.api.ModInitializer

object CasualLobby: ModInitializer {
    override fun onInitialize() {
        LobbyStats.load()

        LobbyData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)
        LobbyParkourData.register(MinigameRegistries.MINIGAME_DATA_MODULE_PROVIDER)
    }
}