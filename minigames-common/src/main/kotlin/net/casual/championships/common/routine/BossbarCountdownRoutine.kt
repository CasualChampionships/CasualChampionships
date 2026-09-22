package net.casual.championships.common.routine

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.virtual.visuals.bossbar.VirtualBossbar
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement

class BossbarCountdownRoutine(
    private val duration: MinecraftTimeDuration,
    private val bossbar: (TimerElement) -> VirtualBossbar
): MinigameRoutine<Minigame> {
    override suspend fun RoutineScope<Minigame>.run() {
        val timer = TimerElement(duration)
        val bossbar = bossbar.invoke(timer)
        try {
            minigame.visuals.addBossbar(bossbar)
            delay(duration, timer::setRemainingDuration)
        } finally {
            minigame.visuals.removeBossbar(bossbar)
        }
    }

    override fun codec(): MapCodec<out Routine<Minigame>> {
        throw UnsupportedOperationException()
    }
}