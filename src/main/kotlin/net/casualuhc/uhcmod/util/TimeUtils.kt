package net.casualuhc.uhcmod.util

import net.casualuhc.arcade.scheduler.MinecraftTimeUnit

object TimeUtils {
    fun formatHHMMSS(time: Number, unit: MinecraftTimeUnit): String {
        val seconds = unit.toSeconds(time.toDouble()).toInt()
        val hours = seconds / 3600
        return "%02d:".format(hours) + this.formatMMSS(time, unit)
    }

    fun formatMMSS(time: Number, unit: MinecraftTimeUnit): String {
        val seconds = unit.toSeconds(time.toDouble()).toInt()
        val minutes = seconds % 3600 / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }
}