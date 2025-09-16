package net.casual.championships.minigame.lobby

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.casual.championships.minigame.duel.DuelArenas
import kotlin.reflect.KProperty0

object LobbyMinigames {
    private val lobbies = CasualUtils.resolve("lobbies")

    fun create(
        lobby: String,
        next: KProperty0<Minigame?>,
        context: MinigameCreationContext
    ): LobbyMinigame {
        val server = context.server
        val modules = try {
            val archive = ReadableArchive.from(this.lobbies, lobby)
            MinigameDataModules.from(archive, server)
        } catch (exception: Exception) {
            CasualUtils.logger.error("Failed to read lobby $lobby", exception)
            MinigameDataModules.empty()
        }
        return LobbyMinigame(server, context.uuid, next, DuelArenas.with(modules))
    }
}