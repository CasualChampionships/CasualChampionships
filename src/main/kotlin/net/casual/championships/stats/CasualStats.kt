package net.casual.championships.stats

import net.casual.arcade.stats.StatType
import net.casual.championships.util.CasualUtils

object CasualStats {
    val MINESWEEPER_RECORD = StatType.float64(CasualUtils.id("minesweeper_record"), Double.NaN)
}