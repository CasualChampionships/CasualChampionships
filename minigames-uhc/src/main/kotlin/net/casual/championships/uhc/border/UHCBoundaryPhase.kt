package net.casual.championships.uhc.border

import net.casual.arcade.boundary.LevelBoundary.SizeAndCenter
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

private fun default(radius: Double): SizeAndCenter {
    return SizeAndCenter(Vec3(radius, 16384.0, radius), Vec3(0.0, 63.0, 0.0))
}

enum class UHCBoundaryPhase(
    val start: SizeAndCenter,
    val end: SizeAndCenter,
    private val duration: MinecraftTimeDuration = MinecraftTimeDuration.ZERO,
    private val cooldown: MinecraftTimeDuration = MinecraftTimeDuration.ZERO
) {
    First(default(6128.0), default(3064.0), 48.Minutes, 10.Minutes), // ~1.05BPS
    Second(First.end, default(1532.0), 26.Minutes, 5.Minutes), // ~1BPS
    Third(Second.end, default(510.0), 18.Minutes, 2.Minutes), // ~0.95BPS
    Fourth(Third.end, default(102.0), 8.Minutes, 1.Minutes), // ~0.85BPS
    Fifth(Fourth.end, default(20.0), 2.Minutes); // ~0.68BPS

    fun getSpeed(): Double {
        val dx = abs(this.start.size.x - this.end.size.x)
        val dz = abs(this.start.size.z - this.end.size.z)
        val avg = (dx + dz) / 2
        return avg / this.duration.ticks
    }

    fun getDuration(total: MinecraftTimeDuration): MinecraftTimeDuration {
        return total * (this.duration / TOTAL_TIME)
    }

    fun getCooldown(total: MinecraftTimeDuration): MinecraftTimeDuration {
        return total * (this.cooldown / TOTAL_TIME)
    }

    fun getNextStage(): UHCBoundaryPhase {
        return entries.getOrElse(this.ordinal + 1) { Fifth }
    }

    companion object {
        // 120 minutes
        val TOTAL_TIME = entries.fold(MinecraftTimeDuration.ZERO) { acc, stage -> acc + stage.duration + stage.cooldown }
    }
}