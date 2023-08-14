package net.casualuhc.uhcmod.util

import net.minecraft.world.entity.EntityType.*
import net.minecraft.world.entity.Mob

object PerformanceUtils {
    private val DISABLED_ENTITIES = setOf(PIG, COW, SQUID, BAT, GLOW_SQUID, FOX, COD, SALMON, PARROT, MOOSHROOM, HORSE, SKELETON_HORSE, CAT, MULE, DONKEY, CHICKEN, SHEEP, GOAT)

    const val MOBCAP_MULTIPLIER = 0.1

    @JvmStatic
    fun isEntityAIDisabled(mob: Mob): Boolean {
        return DISABLED_ENTITIES.contains(mob.type)
    }
}