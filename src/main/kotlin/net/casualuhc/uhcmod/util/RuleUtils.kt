package net.casualuhc.uhcmod.util

import carpet.CarpetServer
import carpet.api.settings.InvalidRuleValueException
import carpet.fakes.SpawnGroupInterface
import carpet.utils.SpawnReporter
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.utils.LevelUtils
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.settings.GameSettings
import net.minecraft.server.MinecraftServer
import net.minecraft.world.Difficulty
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.GameRules
import kotlin.math.ln

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
        LevelUtils.forEachLevel { level ->
            level.worldBorder.setCenter(0.0, 0.0)
            level.worldBorder.size = 6128.0
        }

        GameSettings.PVP.setValue(false)

        try {
            val source = server.createCommandSourceStack()
            val manager = CarpetServer.settingsManager
            manager.getCarpetRule("commandLog").set(source, "ops")
            manager.getCarpetRule("commandDistance").set(source, "ops")
            manager.getCarpetRule("commandInfo").set(source, "ops")
            manager.getCarpetRule("commandPerimeterInfo").set(source, "ops")
            manager.getCarpetRule("commandProfile").set(source, "ops")
            manager.getCarpetRule("commandScript").set(source, "ops")
            manager.getCarpetRule("lightEngineMaxBatchSize").set(source, "500")
            manager.getCarpetRule("structureBlockLimit").set(source, "256")
            manager.getCarpetRule("fillLimit").set(source, "1000000")
            manager.getCarpetRule("fillUpdates").set(source, "false")
            manager.getCarpetRule("commandInfo").set(source, "ops")
            manager.getCarpetRule("commandInfo").set(source, "ops")
        } catch (e: InvalidRuleValueException) {
            UHCMod.logger.error("Failed to set carpet rule", e)
        }
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

        val ratio = 7.0 / (MobCategory.MONSTER as SpawnGroupInterface).initialSpawnCap
        SpawnReporter.mobcap_exponent = 4.0 * ln(ratio) / ln(2.0)
    }
}