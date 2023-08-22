package net.casualuhc.uhc.minigame.uhc

import net.casualuhc.arcade.minigame.MinigamePhase

enum class UHCPhase: MinigamePhase {
    Setup, Lobby, Ready, Start, Active, End;

    override val id: String = name.lowercase()
}