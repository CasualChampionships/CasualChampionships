package net.casual.championships.minigame.uhc.events

import net.casual.arcade.minigame.MinigameResources
import net.casual.arcade.minigame.lobby.Lobby
import net.casual.championships.minigame.uhc.UHCMinigame

@Deprecated("Implement this better!")
interface UHCEvent {
    fun getTeamSize(): Int

    fun getLobby(): Lobby

    fun getResourcePackHandler(): MinigameResources

    fun initialise(uhc: UHCMinigame)
}