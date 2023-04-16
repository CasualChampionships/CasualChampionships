package net.casualuhc.uhcmod.util

object TimeUtils {
    fun secondsToString(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        val secs = seconds % 60
        return "%02d:%02d:%02d".format(hours, minutes, secs)
    }
}