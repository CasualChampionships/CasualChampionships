package net.casual.championships.common.util

import net.casual.arcade.events.entity.EntityStartTrackingEvent
import net.casual.arcade.events.entity.MobCategorySpawnEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.TeamUtils.getOnlineCount
import net.minecraft.world.entity.EntityType.*
import net.minecraft.world.entity.Mob
import kotlin.math.max

object PerformanceUtils {
    private val DISABLED_ENTITIES = setOf(PIG, COW, SQUID, BAT, GLOW_SQUID, FOX, COD, SALMON, PARROT, MOOSHROOM, SKELETON_HORSE, CAT, CHICKEN, SHEEP, GOAT, FROG)

    fun reduceMinigameMobcap(minigame: Minigame<*>) {
        minigame.events.register<MobCategorySpawnEvent> {
            val (_, category, _, state) = it

            state.spawnableChunkCount
            val players = minigame.players.playingPlayerCount
            val mobcap = max(7.0, 2_500 / (players + 25).toDouble() - 30)

            if (state.mobCategoryCounts.getInt(category) >= mobcap * players) {
                it.cancel()
            }
        }
    }

    fun disableEntityAI(minigame: Minigame<*>) {
        minigame.events.register<EntityStartTrackingEvent> {
            val entity = it.entity
            if (entity is Mob && DISABLED_ENTITIES.contains(entity.type)) {
                entity.isNoAi = true
            }
        }
    }
}