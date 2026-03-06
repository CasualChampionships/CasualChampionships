package net.casual.championships.common.util

import net.casual.arcade.minigame.stats.StatType
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.minecraft.core.Holder
import net.minecraft.core.Registry

object CasualStats {
    val WON = this.register("won", StatType.bool())
    val ALIVE_TIME = this.register("alive_time", StatType.int32())
    val CROUCH_TIME = this.register("crouch_time", StatType.int32())
    val DIORITE_TIME = this.register("diorite_time", StatType.int32(-1))
    val JUMPS = this.register("jumps", StatType.int32())
    val BLOCKS_MINED = this.register("blocks_mined", StatType.int32())
    val BLOCKS_PLACED = this.register("blocks_placed",  StatType.int32())

    internal fun load() {

    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> register(name: String, type: StatType<T>): Holder.Reference<StatType<T>> {
        return Registry.registerForHolder(MinigameRegistries.STAT_TYPES, casual(name), type)
            as Holder.Reference<StatType<T>>
    }
}