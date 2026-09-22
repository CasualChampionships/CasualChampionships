package net.casual.championships.uhc.minigame.phase

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.resources.Identifier

class GameplayRoutine: MinigameRoutine<UHCMinigame> {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() {
        awaitCancellation()
    }

    companion object: CodecProvider<GameplayRoutine> {
        override val id: Identifier = casual("uhc_gameplay")
        override val codec: MapCodec<out GameplayRoutine> = MapCodec.unit(::GameplayRoutine)
    }
}