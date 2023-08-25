package net.casual.minigame.uhc

import net.casual.arcade.minigame.MinigamePhase

enum class UHCPhase: MinigamePhase {
    Setup, Lobby, Ready, Start, Active, End;

    override val id: String = name.lowercase()
}