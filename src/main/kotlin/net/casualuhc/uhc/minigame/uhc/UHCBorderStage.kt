package net.casualuhc.uhc.minigame.uhc

import net.minecraft.world.level.Level

enum class UHCBorderStage(
    private val startSize: Double,
    private val endSize: Double,
    private val weight: Double
) {
    FIRST(6128.0, 3064.0, 18.0),
    SECOND(FIRST.endSize, 1500.0, 15.0),
    THIRD(SECOND.endSize, 780.0, 9.0),
    FOURTH(THIRD.endSize, 400.0, 8.0),
    FIFTH(FOURTH.endSize, 220.0, 7.0),
    SIX(FIFTH.endSize, 50.0, 5.0),
    FINAL(SIX.endSize, 20.0, 2.0),
    END(FINAL.endSize, 0.0, 0.0);

    fun getRemainingTimeAsPercent(size: Double, level: Level): Double {
        val remainingSize = size - this.getEndSizeFor(level)
        val totalSize = this.getStartSizeFor(level) - this.getEndSizeFor(level)
        return (remainingSize / totalSize) * (this.weight / TOTAL_WEIGHT)
    }

    fun getNextStage(): UHCBorderStage {
        val next = this.ordinal + 1
        val values = UHCBorderStage.values()
        return if (next < values.size) values[next] else END
    }

    fun getStartSizeFor(level: Level): Double {
        return adjustSize(this.startSize, level)
    }

    fun getEndSizeFor(level: Level): Double {
        return adjustSize(this.endSize, level)
    }

    companion object {
        val TOTAL_WEIGHT = UHCBorderStage.values().sumOf { it.weight }

        fun getStage(size: Double, level: Level): UHCBorderStage? {
            val adjusted = reverseAdjustSize(size, level)
            if (adjusted <= FINAL.endSize) {
                return END
            }
            for (stage in UHCBorderStage.values()) {
                if (adjusted <= stage.startSize && adjusted > stage.endSize) {
                    return stage
                }
            }
            return null
        }

        fun adjustSize(size: Double, level: Level): Double {
            return size / level.dimensionType().coordinateScale
        }

        fun reverseAdjustSize(size: Double, level: Level): Double {
            return size * level.dimensionType().coordinateScale
        }
    }
}