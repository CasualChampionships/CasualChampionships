package net.casual.championships.uhc.utils

import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.championships.uhc.utils.UHCDimensions.LevelWithPersistence

class UHCDimensions(
    val overworld: LevelWithPersistence,
    val nether: LevelWithPersistence,
    val end: LevelWithPersistence
): Iterable<LevelWithPersistence> {
    override fun iterator(): Iterator<LevelWithPersistence> {
        return listOf(this.overworld, this.nether, this.end).iterator()
    }

    data class LevelWithPersistence(val level: CustomLevel, val persist: Boolean)
}