package net.casual.championships.config

import net.casual.arcade.minigame.lobby.Lobby
import java.util.UUID

data class Event(
    val minigame: UUID?,
    val teamSize: Int,
    val operators: List<String>,
    val lobby: Lobby,
    val resources: EventResources
)

data class EventResources(
    val hostIp: String,
    val hostPort: Int,
    val packs: List<String>
)