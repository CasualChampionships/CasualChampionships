package net.casual.championships.minigame.lobby

import net.casual.arcade.minigame.stats.StatType
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.championships.CasualMod
import net.minecraft.core.Holder
import net.minecraft.core.Registry

object LobbyStats {
    val MINESWEEPER_RECORD = this.register("minesweeper_record", StatType.int32(Int.MAX_VALUE))
    val MINESWEEPER_RECORD_HELD = this.register("minesweeper_record_ticks", StatType.int32())
    val LEFT_LOBBY = this.register("left_lobby", StatType.int32())
    val ATTACK_ADMIN = this.register("attack_admin", StatType.int32())

    internal fun load() {

    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> register(name: String, type: StatType<T>): Holder.Reference<StatType<T>> {
        return Registry.registerForHolder(MinigameRegistries.STAT_TYPES, CasualMod.id(name), type)
            as Holder.Reference<StatType<T>>
    }
}