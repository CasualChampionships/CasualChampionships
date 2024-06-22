package net.casual.championships.uhc

import net.casual.arcade.stats.StatType

object UHCStats {
    val HALF_HEART_TIME = StatType.int32(UHCMod.id("half_heart_time"))
    val WORLD_BORDER_TIME = StatType.int32(UHCMod.id("world_border_time"))
    val HEADS_CONSUMED = StatType.int32(UHCMod.id("heads_consumed"))
    val ADVANCEMENTS_AWARDED = StatType.int32(UHCMod.id("advancements_awarded"))
}