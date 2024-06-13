package net.casual.championships.common.util

import net.casual.arcade.stats.StatType
import net.casual.championships.common.CommonMod

object CommonStats {
    val MINESWEEPER_RECORD = StatType.float64(CommonMod.id("minesweeper_record"), Double.NaN)
    val WON = StatType.bool(CommonMod.id("won"))

    val ALIVE_TIME = StatType.int32(CommonMod.id("alive_time"))
    val CROUCH_TIME = StatType.int32(CommonMod.id("crouch_time"))
    val JUMPS = StatType.int32(CommonMod.id("jumps"))
    val BLOCKS_MINED = StatType.int32(CommonMod.id("blocks_mined"))
    val BLOCKS_PLACED = StatType.int32(CommonMod.id("blocks_placed"))
}