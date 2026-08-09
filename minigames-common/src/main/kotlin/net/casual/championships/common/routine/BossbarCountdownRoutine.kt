package net.casual.championships.common.routine

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.task.routine.MinigameRoutine
import net.casual.arcade.minigame.task.routine.minigame
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.virtual.visuals.bossbar.VirtualBossbar
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement

abstract class BossbarCountdownRoutine<M: Minigame>(
    private val duration: MinecraftTimeDuration
): MinigameRoutine<M> {
    final override suspend fun RoutineScope<M>.run() {
        val timer = TimerElement(duration)
        val bossbar = createBossbar(minigame, timer)
        try {
            minigame.visuals.addBossbar(bossbar)
            delay(duration, timer::setRemainingDuration)
            afterCountdown()
        } finally {
            minigame.visuals.removeBossbar(bossbar)
        }
    }

    protected abstract fun createBossbar(minigame: M, timer: TimerElement): VirtualBossbar

    protected open suspend fun RoutineScope<M>.afterCountdown() {

    }

    protected companion object {
        fun <R: BossbarCountdownRoutine<*>> codec(factory: (MinecraftTimeDuration) -> R): MapCodec<R> {
            return MinecraftTimeDuration.Companion.CODEC.fieldOf("duration").xmap(factory, BossbarCountdownRoutine<*>::duration)
        }
    }
}