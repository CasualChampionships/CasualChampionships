package net.casual.minigame.uhc.events

import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.minigame.uhc.UHCMinigame

interface UHCEvent {
    fun getTeamSize(): Int

    fun getLobby(): Lobby

    fun getResourcePackHandler(): MinigameResources

    fun initialise(uhc: UHCMinigame)
}