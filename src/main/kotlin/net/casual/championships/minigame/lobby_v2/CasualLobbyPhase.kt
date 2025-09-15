package net.casual.championships.minigame.lobby_v2

import net.casual.arcade.minigame.phase.Phase
import net.minecraft.world.scores.Team

enum class CasualLobbyPhase(override val id: String): Phase<CasualLobbyMinigame> {
    Waiting("waiting") {
        override fun initialize(minigame: CasualLobbyMinigame) {
            minigame.ui.addBossbar(minigame.bossbar)
            for (team in minigame.teams.getAllTeams()) {
                team.collisionRule = Team.CollisionRule.NEVER
            }
        }
    },
    Readying("readying"),
    Countdown("countdown") {
        override fun start(minigame: CasualLobbyMinigame, previous: Phase<CasualLobbyMinigame>) {
            minigame.ui.removeBossbar(minigame.bossbar)
            for (team in minigame.teams.getAllTeams()) {
                team.collisionRule = Team.CollisionRule.ALWAYS
            }
            minigame.startCountdown()
        }
    }
}