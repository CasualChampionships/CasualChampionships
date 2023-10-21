package net.casual.util

import net.minecraft.server.MinecraftServer
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules.*

object RuleUtils {
    fun setLobbyGamerules(server: MinecraftServer) {
        val rules = server.gameRules
        rules.getRule(RULE_NATURAL_REGENERATION).set(true, server)
        rules.getRule(RULE_DOINSOMNIA).set(false, server)
        rules.getRule(RULE_DOFIRETICK).set(false, server)
        rules.getRule(RULE_DAYLIGHT).set(false, server)
        rules.getRule(RULE_ANNOUNCE_ADVANCEMENTS).set(true, server)
        rules.getRule(RULE_FALL_DAMAGE).set(false, server)
        rules.getRule(RULE_DROWNING_DAMAGE).set(false, server)
        rules.getRule(RULE_DOENTITYDROPS).set(false, server)
        rules.getRule(RULE_WEATHER_CYCLE).set(false, server)
        rules.getRule(RULE_DO_TRADER_SPAWNING).set(false, server)
        rules.getRule(RULE_DOBLOCKDROPS).set(false, server)
        rules.getRule(RULE_SNOW_ACCUMULATION_HEIGHT).set(0, server)
        rules.getRule(RULE_RANDOMTICKING).set(0, server)
        server.setDifficulty(Difficulty.PEACEFUL, true)
        server.overworld().apply {
            dayTime = 6000
            setWeatherParameters(999999, 0, false, false)
        }
    }

    fun setActiveGamerules(server: MinecraftServer) {
        val rules = server.gameRules
        rules.getRule(RULE_NATURAL_REGENERATION).set(false, server)
        rules.getRule(RULE_DOFIRETICK).set(true, server)
        rules.getRule(RULE_DAYLIGHT).set(true, server)
        rules.getRule(RULE_FALL_DAMAGE).set(true, server)
        rules.getRule(RULE_DROWNING_DAMAGE).set(true, server)
        rules.getRule(RULE_DOENTITYDROPS).set(true, server)
        rules.getRule(RULE_WEATHER_CYCLE).set(true, server)
        rules.getRule(RULE_DO_TRADER_SPAWNING).set(true, server)
        rules.getRule(RULE_DOBLOCKDROPS).set(true, server)
        rules.getRule(RULE_SNOW_ACCUMULATION_HEIGHT).set(1, server)
        rules.getRule(RULE_RANDOMTICKING).set(3, server)
        server.setDifficulty(Difficulty.HARD, true)
        server.overworld().dayTime = 0
    }
}