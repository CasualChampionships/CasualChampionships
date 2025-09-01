package net.casual.championships.uhc.utils

import net.casual.arcade.minigame.stats.StatType
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.championships.uhc.UHCMod
import net.minecraft.core.Holder
import net.minecraft.core.Registry

object UHCStats {
    val HALF_HEART_TIME = this.register("half_heart_time", StatType.int32())
    val HEADS_CONSUMED = this.register("heads_consumed", StatType.int32())
    val ADVANCEMENTS_AWARDED = this.register("advancements_awarded", StatType.int32())

    val LAST_SNEAK_TIME = this.register("last_sneak_time", StatType.int32())

    val NETHER_WART_MINED = this.register("nether_wart_mined", StatType.int32())

    internal fun noop() {

    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> register(name: String, type: StatType<T>): Holder.Reference<StatType<T>> {
        return Registry.registerForHolder(MinigameRegistries.STAT_TYPES, UHCMod.id(name), type)
            as Holder.Reference<StatType<T>>
    }
}