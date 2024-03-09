package net.casual.championships.common.util

import net.casual.arcade.stats.StatType
import net.casual.championships.common.CasualCommonMod

object CommonStats {
    val MINESWEEPER_RECORD = StatType.float64(CasualCommonMod.id("minesweeper_record"), Double.NaN)
}