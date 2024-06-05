package net.casual.championships.uhc.border

import net.minecraft.world.level.Level

enum class UHCBorderStage(
    private val startSize: Double,
    private val endSize: Double,
    private val weight: Double
) {
    First(6128.0, 3064.0, 18.0),
    Second(First.endSize, 1500.0, 15.0),
    Third(Second.endSize, 780.0, 9.0),
    Fourth(Third.endSize, 400.0, 8.0),
    Fifth(Fourth.endSize, 220.0, 7.0),
    Sixth(Fifth.endSize, 50.0, 5.0),
    Final(Sixth.endSize, 20.0, 2.0),
    End(Final.endSize, 0.0, 0.0);

    fun getRemainingTimeAsPercent(size: Double, level: Level, multiplier: Double): Double {
        val remainingSize = size - this.getEndSizeFor(level, multiplier)
        val totalSize = this.getStartSizeFor(level, multiplier) - this.getEndSizeFor(level, multiplier)
        return (remainingSize / totalSize) * (this.weight / TOTAL_WEIGHT)
    }

    fun getNextStage(): UHCBorderStage {
        val next = this.ordinal + 1
        return if (next < entries.size) entries[next] else End
    }

    fun getStartSizeFor(level: Level, multiplier: Double): Double {
        return adjustSize(this.startSize, level) * multiplier
    }

    fun getEndSizeFor(level: Level, multiplier: Double): Double {
        return adjustSize(this.endSize, level) * multiplier
    }

    companion object {
        val TOTAL_WEIGHT = entries.sumOf { it.weight }

        fun adjustSize(size: Double, level: Level): Double {
            return size / level.dimensionType().coordinateScale
        }
    }
}