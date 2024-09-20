package net.casual.championships.util

import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.casual.championships.minigame.CasualChampionshipsTemplate
import net.casual.championships.minigame.lobby.CasualBossbarTemplate
import net.casual.championships.minigame.lobby.CasualCountdownTemplate
import net.casual.championships.minigame.lobby.CasualLobbyTemplate

object CasualRegistration {
    fun register() {
        CasualCountdownTemplate.register(MinigameRegistries.COUNTDOWN_TEMPLATE)
        CasualBossbarTemplate.register(MinigameRegistries.TIMER_BOSSBAR_TEMPLATE)
        CasualLobbyTemplate.register(MinigameRegistries.LOBBY_TEMPLATE)
        CasualChampionshipsTemplate.register(MinigameRegistries.MINIGAMES_EVENT)
    }
}