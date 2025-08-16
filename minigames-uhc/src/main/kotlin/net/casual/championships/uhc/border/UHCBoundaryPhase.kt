package net.casual.championships.uhc.border

import net.casual.arcade.boundary.LevelBoundary
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.utils.time.MinecraftTimeUnit
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

sealed class UHCBoundaryPhase(
    private val ordinal: Int,
    private val duration: MinecraftTimeDuration,
    private val cooldown: MinecraftTimeDuration
): Comparable<UHCBoundaryPhase> {
    fun getStart(level: ServerLevel): LevelBoundary.SizeAndCenter {
        return LevelBoundary.SizeAndCenter(this.getStartSize(level), this.getStartCenter(level))
    }

    fun getEnd(level: ServerLevel): LevelBoundary.SizeAndCenter {
        return LevelBoundary.SizeAndCenter(this.getEndSize(level), this.getEndCenter(level))
    }

    fun getSpeedInBlocksPerTick(level: ServerLevel): Double {
        val start = this.getStartSize(level)
        val end = this.getEndSize(level)
        val dx = abs(start.x - end.x)
        val dz = abs(start.z - end.z)
        val avg = (dx + dz) / 2
        return avg / this.duration.ticks
    }

    fun getDuration(total: MinecraftTimeDuration): MinecraftTimeDuration {
        return total * (this.duration.ticks.toDouble() / TOTAL_TIME.ticks)
    }

    fun getCooldown(total: MinecraftTimeDuration): MinecraftTimeDuration {
        return total * (this.cooldown.ticks.toDouble() / TOTAL_TIME.ticks)
    }

    override fun compareTo(other: UHCBoundaryPhase): Int {
        return this.ordinal.compareTo(other.ordinal)
    }

    abstract fun getStartSize(level: ServerLevel): Vec3
    abstract fun getEndSize(level: ServerLevel): Vec3
    abstract fun getStartCenter(level: ServerLevel): Vec3
    abstract fun getEndCenter(level: ServerLevel): Vec3
    abstract fun getNextStage(): UHCBoundaryPhase

    data object First: UHCBoundaryPhase(0, 48.Minutes, 8.Minutes) {
        override fun getStartSize(level: ServerLevel) = Vec3(6128.0, 1024.0, 6128.0)
        override fun getEndSize(level: ServerLevel) = Vec3(3064.0, 1024.0, 3064.0)
        override fun getStartCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getEndCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getNextStage() = Second
    }

    data object Second: UHCBoundaryPhase(1, 26.Minutes, 5.Minutes) {
        override fun getStartSize(level: ServerLevel) = First.getEndSize(level)
        override fun getEndSize(level: ServerLevel) = Vec3(1532.0, 1024.0, 1532.0)
        override fun getStartCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getEndCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getNextStage() = Third
    }

    data object Third: UHCBoundaryPhase(2, 18.Minutes, 2.Minutes) {
        override fun getStartSize(level: ServerLevel) = Second.getEndSize(level)
        override fun getEndSize(level: ServerLevel) = Vec3(510.0, 1024.0, 510.0)
        override fun getStartCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getEndCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getNextStage() = Fourth
    }

    data object Fourth: UHCBoundaryPhase(3, 6.Minutes, 1.Minutes) {
        override fun getStartSize(level: ServerLevel) = Third.getEndSize(level)
        override fun getEndSize(level: ServerLevel) = Vec3(102.0, 1024.0, 102.0)
        override fun getStartCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getEndCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getNextStage() = Fifth
    }

    data object Fifth: UHCBoundaryPhase(4, 2.Minutes, 2.Minutes) {
        override fun getStartSize(level: ServerLevel) = Fourth.getEndSize(level)
        override fun getEndSize(level: ServerLevel) = Vec3(20.0, 1024.0, 20.0)
        override fun getStartCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getEndCenter(level: ServerLevel) = DEFAULT_CENTER
        override fun getNextStage() = Sixth
    }

    data object Sixth: UHCBoundaryPhase(5, 6.Minutes, 0.Minutes) {
        override fun getStartSize(level: ServerLevel): Vec3 {
            val y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0).toDouble()
            val height = ((y - level.minY) + 10) * 2
            return Fifth.getEndSize(level).with(Direction.Axis.Y, height)
        }

        override fun getEndSize(level: ServerLevel): Vec3 {
            return Fifth.getEndSize(level).with(Direction.Axis.Y, 60.0)
        }

        override fun getStartCenter(level: ServerLevel): Vec3 {
            val y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, 0, 0).toDouble()
            return Fifth.getEndCenter(level).with(Direction.Axis.Y, y)
        }

        override fun getEndCenter(level: ServerLevel): Vec3 {
            return this.getStartCenter(level)
        }

        override fun getNextStage() = Sixth
    }

    companion object {
        private val DEFAULT_CENTER = Vec3(0.0, 63.0, 0.0)

        val entries by lazy { listOf(First, Second, Third, Fourth, Fifth, Sixth) }

        val TOTAL_TIME by lazy {
            this.entries.fold(MinecraftTimeDuration.ZERO) { acc, stage ->
                acc + stage.duration + stage.cooldown
            }
        }
    }
}