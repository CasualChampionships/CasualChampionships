package net.casual.championships.util

import net.casual.arcade.utils.registries.ArcadeRegistries
import net.casual.arcade.utils.serialization.CodecProvider.Companion.register
import net.casual.championships.minigame.CasualChampionshipsEvent
import net.casual.championships.minigame.lobby.CasualBossbarTemplate
import net.casual.championships.minigame.lobby.CasualCountdownTemplate
import net.casual.championships.minigame.lobby.CasualLobbyTemplate

object CasualRegistration {
    fun register() {
        CasualCountdownTemplate.register(ArcadeRegistries.COUNTDOWN_TEMPLATE)
        CasualBossbarTemplate.register(ArcadeRegistries.TIMER_BOSSBAR_TEMPLATE)
        CasualLobbyTemplate.register(ArcadeRegistries.LOBBY_TEMPLATE)
        CasualChampionshipsEvent.register(ArcadeRegistries.MINIGAMES_EVENT)
    }
}