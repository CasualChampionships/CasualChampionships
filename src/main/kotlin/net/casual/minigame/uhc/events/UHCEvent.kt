package net.casual.minigame.uhc.events

import net.casual.arcade.minigame.MinigameLobby
import net.casual.arcade.minigame.MinigameResources
import net.casual.minigame.uhc.UHCMinigame

interface UHCEvent {
    fun getTeamSize(): Int

    fun getMinigameLobby(): MinigameLobby

    fun getResourcePackHandler(): MinigameResources

    fun initialise(uhc: UHCMinigame)
}