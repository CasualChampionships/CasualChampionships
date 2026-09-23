package net.casual.championships.uhc.boundary

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.utils.call
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.routine.GlowingCountdownRoutine
import net.minecraft.resources.Identifier

class UHCBoundaryRoutine: Routine<UHCMinigame> {
    override val version: Int
        get() = 2

    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() {
        step("start") { minigame.boundary.onStarted() }

        var phase: UHCBoundaryPhase = UHCBoundaryPhase.First
        while (true) {
            step("move-${phase.name}") { minigame.boundary.moveTo(phase) }
            delay(phase.getDuration(minigame.settings.borderTime))

            val next = phase.next() ?: break
            step("pause-${phase.name}") { minigame.boundary.onPaused() }
            val cooldown = phase.getCooldown(minigame.settings.borderTime)
            if (phase == UHCBoundaryPhase.Fifth) {
                call(GlowingCountdownRoutine(cooldown))
            } else {
                delay(cooldown)
            }
            step("resume-${phase.name}") { minigame.boundary.onResumed() }
            phase = next
        }

        step("complete") { minigame.boundary.onCompleted() }
    }

    companion object: CodecProvider<UHCBoundaryRoutine> {
        override val id: Identifier = casual("uhc_boundary")
        override val codec: MapCodec<out UHCBoundaryRoutine> = MapCodec.unit(::UHCBoundaryRoutine)
    }
}
