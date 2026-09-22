package net.casual.championships.common.minigame.rules

// TODO: Move to a component
interface MinigameRulesProvider {
    fun getRules(): MinigameRules
}