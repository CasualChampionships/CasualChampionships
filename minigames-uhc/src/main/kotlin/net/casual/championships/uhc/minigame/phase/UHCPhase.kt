package net.casual.championships.uhc.minigame.phase

import net.casual.arcade.minigame.phase.MinigamePhase

enum class UHCPhase: MinigamePhase {
    Initializing,
    Grace,
    Gameplay,
    GameOver
}