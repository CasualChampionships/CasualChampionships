package net.casual.championships.lobby.minigame

import net.casual.arcade.minigame.phase.Phase
import net.minecraft.world.scores.Team

enum class LobbyPhase(override val id: String): Phase<LobbyMinigame> {
    Waiting("waiting") {
        override fun initialize(minigame: LobbyMinigame) {
            minigame.visuals.addBossbar(minigame.bossbar)
            for (team in minigame.teams.getAllTeams()) {
                team.collisionRule = Team.CollisionRule.NEVER
            }
        }
    },
    Readying("readying"),
    Countdown("countdown") {
        override fun start(minigame: LobbyMinigame, previous: Phase<LobbyMinigame>) {
            minigame.visuals.removeBossbar(minigame.bossbar)
            for (team in minigame.teams.getAllTeams()) {
                team.collisionRule = Team.CollisionRule.ALWAYS
            }
            minigame.startCountdown()
        }
    }
}