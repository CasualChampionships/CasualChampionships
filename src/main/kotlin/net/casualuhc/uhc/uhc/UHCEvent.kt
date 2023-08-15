package net.casualuhc.uhc.uhc

import net.casualuhc.uhc.uhc.handlers.LobbyHandler
import net.casualuhc.uhc.uhc.handlers.ResourceHandler

interface UHCEvent {
    fun getTeamSize(): Int

    fun getLobbyHandler(): LobbyHandler

    fun getResourcePackHandler(): ResourceHandler

    fun initialise()
}