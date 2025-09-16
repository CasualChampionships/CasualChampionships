package net.casual.championships.lobby

import net.casual.championships.lobby.stats.LobbyStats
import net.fabricmc.api.ModInitializer

object CasualLobby: ModInitializer {
    override fun onInitialize() {
        LobbyStats.load()
    }
}