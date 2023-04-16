package net.casualuhc.uhcmod.uhc

import net.casualuhc.uhcmod.uhc.handlers.BossBarHandler
import net.casualuhc.uhcmod.uhc.handlers.LobbyHandler
import net.casualuhc.uhcmod.uhc.handlers.ResourceHandler

interface UHCEvent {
    fun getName(): String

    fun getTeamSize(): Int

    fun getLobbyHandler(): LobbyHandler

    fun getBossBarHandler(): BossBarHandler

    fun getResourcePackHandler(): ResourceHandler

    fun load()
}