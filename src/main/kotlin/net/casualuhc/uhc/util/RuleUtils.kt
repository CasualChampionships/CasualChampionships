package net.casualuhc.uhc.util

import net.casualuhc.arcade.Arcade
import net.casualuhc.uhc.managers.WorldBorderManager
import net.casualuhc.uhc.settings.GameSettings
import net.minecraft.server.MinecraftServer
import net.minecraft.world.Difficulty
import net.minecraft.world.level.GameRules

object RuleUtils {
    fun setLobbyGamerules() {
        val server = Arcade.server
        val rules: GameRules = server.gameRules
        rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(true, server)
        rules.getRule(GameRules.RULE_DOINSOMNIA).set(false, server)
        rules.getRule(GameRules.RULE_DOFIRETICK).set(false, server)
        rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server)
        rules.getRule(GameRules.RULE_ANNOUNCE_ADVANCEMENTS).set(true, server)
        rules.getRule(GameRules.RULE_FALL_DAMAGE).set(false, server)
        rules.getRule(GameRules.RULE_DROWNING_DAMAGE).set(false, server)
        rules.getRule(GameRules.RULE_DOENTITYDROPS).set(false, server)
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server)
        rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, server)
        rules.getRule(GameRules.RULE_DOBLOCKDROPS).set(false, server)
        rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(0, server)
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, server)
        server.setDifficulty(Difficulty.PEACEFUL, true)
        server.overworld().apply {
            dayTime = 6000
            setWeatherParameters(999999, 0, false, false)
        }

        WorldBorderManager.moveWorldBorders(WorldBorderManager.Stage.FIRST, WorldBorderManager.Size.START, true)

        GameSettings.PVP.setValue(false)
    }

    fun setActiveGamerules() {
        val server: MinecraftServer = Arcade.server
        val rules = server.gameRules
        rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, server)
        rules.getRule(GameRules.RULE_DOFIRETICK).set(true, server)
        rules.getRule(GameRules.RULE_DAYLIGHT).set(true, server)
        rules.getRule(GameRules.RULE_FALL_DAMAGE).set(true, server)
        rules.getRule(GameRules.RULE_DROWNING_DAMAGE).set(true, server)
        rules.getRule(GameRules.RULE_DOENTITYDROPS).set(true, server)
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(true, server)
        rules.getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(true, server)
        rules.getRule(GameRules.RULE_DOBLOCKDROPS).set(true, server)
        rules.getRule(GameRules.RULE_SNOW_ACCUMULATION_HEIGHT).set(1, server)
        rules.getRule(GameRules.RULE_RANDOMTICKING).set(3, server)
        server.setDifficulty(Difficulty.HARD, true)
        server.overworld().dayTime = 0
    }
}