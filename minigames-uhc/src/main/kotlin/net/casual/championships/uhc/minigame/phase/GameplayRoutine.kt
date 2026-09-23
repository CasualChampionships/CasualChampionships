package net.casual.championships.uhc.minigame.phase

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.common.util.CasualSounds
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.resources.Identifier

class GameplayRoutine: MinigameRoutine<UHCMinigame> {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() {
        step {
            minigame.chat.broadcastGame(
                CasualComponents.BORDER_GRACE_OVER.red().withMiniFont(),
                sound = Sound(CasualSounds.GAME_BORDER_MOVING)
            )
            minigame.settings.canPvp.set(true)
        }
        awaitCancellation()
    }

    companion object: CodecProvider<GameplayRoutine> {
        override val id: Identifier = casual("uhc_gameplay")
        override val codec: MapCodec<out GameplayRoutine> = MapCodec.unit(::GameplayRoutine)
    }
}
