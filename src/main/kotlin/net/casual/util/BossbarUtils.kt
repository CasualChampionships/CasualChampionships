package net.casual.util

object BossbarUtils {
    fun shrink(percent: Float, factor: Float): Float {
        val shift = (1 - factor) / 2.0F
        return shift + percent * factor
    }
}