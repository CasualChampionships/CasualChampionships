package net.casual.championships.uhc.routine

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.task.routine.minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.CasualUHC
import net.casual.championships.uhc.border.UHCBoundaryManager
import net.casual.championships.uhc.border.UHCBoundaryPhase
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.resources.Identifier

class BoundaryResumeRoutine(
    val current: UHCBoundaryPhase
): Routine<UHCMinigame> {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() {
        if (minigame.boundaryPhase != current) {
            CasualUHC.logger.error("Mismatched boundary phase while resuming, expected $current, got ${minigame.boundaryPhase}")
            return
        }
        minigame.onResumeBoundaryTimer()
        UHCBoundaryManager.move(minigame, current.getNextStage())
    }

    companion object: CodecProvider<BoundaryResumeRoutine> {
        override val id: Identifier = casual("uhc_boundary_resume")
        override val codec: MapCodec<out BoundaryResumeRoutine> = UHCBoundaryPhase.CODEC.fieldOf("phase")
            .xmap(::BoundaryResumeRoutine, BoundaryResumeRoutine::current)
    }
}