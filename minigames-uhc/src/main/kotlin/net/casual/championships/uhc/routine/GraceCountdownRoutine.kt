package net.casual.championships.uhc.routine

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.task.routine.PhaseChangeRoutine
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.utils.call
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.virtual.visuals.bossbar.VirtualBossbar
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement
import net.casual.championships.common.routine.BossbarCountdownRoutine
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.ui.bossbar.GraceBossbar
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.minigame.UHCPhase
import net.minecraft.resources.Identifier

class GraceCountdownRoutine(
    duration: MinecraftTimeDuration
): BossbarCountdownRoutine<UHCMinigame>(duration) {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override fun createBossbar(
        minigame: UHCMinigame,
        timer: TimerElement
    ): VirtualBossbar {
        return GraceBossbar.create(minigame.server, timer)
    }

    override suspend fun RoutineScope<UHCMinigame>.afterCountdown() {
        call(PhaseChangeRoutine(UHCPhase.Gameplay))
    }

    companion object: CodecProvider<GraceCountdownRoutine> {
        override val id: Identifier = casual("uhc_grace_countdown")
        override val codec: MapCodec<out GraceCountdownRoutine> = codec(::GraceCountdownRoutine)
    }
}