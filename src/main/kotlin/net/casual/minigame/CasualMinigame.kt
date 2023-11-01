package net.casual.minigame

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.managers.TeamManager
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.events.UHCEvents
import net.minecraft.server.MinecraftServer

object CasualMinigame {
    private var minigame: Minigame<*>? = null

    fun getCurrent(): Minigame<*> {
        return this.minigame!!
    }

    fun setLobby(server: MinecraftServer, next: Minigame<*>? = null): CasualLobbyMinigame {
        val lobby = CasualLobbyMinigame(server, UHCEvents.getUHC().getLobby())
        this.setNewMinigameAndStart(lobby)
        if (next != null) {
            lobby.setNextMinigame(next)
        }
        return lobby
    }

    fun setNewMinigameAndStart(minigame: Minigame<*>) {
        val current = this.minigame
        if (current != null) {
            for (player in current.getPlayers()) {
                minigame.addPlayer(player)
            }
            current.close()
        }
        this.minigame = minigame
        minigame.start()
    }

    internal fun registerEvents() {
        Minigames.registerFactory("casual_uhc") { UHCMinigame(it) }

        GlobalEventHandler.register<ServerLoadedEvent>(0) {
            this.setLobby(it.server, UHCMinigame(it.server))
        }
        GlobalEventHandler.register<PlayerJoinEvent>(0) {
            this.getCurrent().addPlayer(it.player)
        }
    }
}