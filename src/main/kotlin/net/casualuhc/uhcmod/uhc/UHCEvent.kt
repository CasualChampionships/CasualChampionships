package net.casualuhc.uhcmod.uhc

import net.casualuhc.uhcmod.uhc.handlers.LobbyHandler
import net.casualuhc.uhcmod.uhc.handlers.ResourceHandler

interface UHCEvent {
    fun getTeamSize(): Int

    fun getLobbyHandler(): LobbyHandler

    fun getResourcePackHandler(): ResourceHandler

    fun initialise()
}