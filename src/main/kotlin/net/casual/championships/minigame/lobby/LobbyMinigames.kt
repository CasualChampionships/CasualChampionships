package net.casual.championships.minigame.lobby

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.data.MinigameDataSet
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.lobby.minigame.LobbyMinigame
import net.casual.championships.minigame.duel.DuelArenas
import net.casual.championships.minigame.duel.DuelKits
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
            MinigameDataSet.from(archive, server)
        } catch (exception: Exception) {
            CasualUtils.logger.error("Failed to read lobby $lobby", exception)
            MinigameDataSet.empty()
        }
        return LobbyMinigame(server, context.uuid, next, DuelKits.with(DuelArenas.with(modules)))
    }
}