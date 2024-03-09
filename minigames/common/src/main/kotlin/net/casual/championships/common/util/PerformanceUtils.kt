package net.casual.championships.common.util

import net.casual.arcade.events.entity.EntityStartTrackingEvent
import net.casual.arcade.events.entity.MobCategorySpawnEvent
import net.casual.arcade.minigame.Minigame
import net.minecraft.world.entity.EntityType.*
import net.minecraft.world.entity.Mob

object PerformanceUtils {
    private val DISABLED_ENTITIES = setOf(PIG, COW, SQUID, BAT, GLOW_SQUID, FOX, COD, SALMON, PARROT, MOOSHROOM, SKELETON_HORSE, CAT, CHICKEN, SHEEP, GOAT, FROG)
    private const val MOBCAP_MULTIPLIER = 0.1

    fun reduceMinigameMobcap(minigame: Minigame<*>) {
        minigame.events.register<MobCategorySpawnEvent> {
            val (_, category, _, state) = it
            val i = category.maxInstancesPerChunk * state.spawnableChunkCount / 289
            if (state.mobCategoryCounts.getInt(category) >= i * MOBCAP_MULTIPLIER) {
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