package net.casual.championships.common.level

import net.casual.arcade.dimensions.level.spawner.CustomMobSpawningRules
import net.minecraft.world.entity.MobCategory

object ReducedMobSpawningRules: CustomMobSpawningRules {
    override fun getChunkMobCapFor(category: MobCategory): Int {
        return (super.getChunkMobCapFor(category) * 0.1).toInt()
    }
}