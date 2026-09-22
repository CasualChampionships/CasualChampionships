package net.casual.championships.duel.minigame

import net.casual.arcade.minigame.phase.MinigamePhase

enum class DuelPhase: MinigamePhase {
    Initializing,
    Countdown,
    Dueling,
    Complete
}