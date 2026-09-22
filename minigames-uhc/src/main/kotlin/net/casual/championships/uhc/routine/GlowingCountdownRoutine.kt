package net.casual.championships.uhc.routine

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.utils.call
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.championships.common.routine.BossbarCountdownRoutine
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.ui.bossbar.GlowingBossBar
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.resources.Identifier

class GlowingCountdownRoutine(
    private val duration: MinecraftTimeDuration
): MinigameRoutine<UHCMinigame> {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() {
        call(BossbarCountdownRoutine(duration) { timer -> GlowingBossBar.create(minigame.server, timer) })
        minigame.weAreInTheEndgameNow()
    }

    companion object: CodecProvider<GlowingCountdownRoutine> {
        override val id: Identifier = casual("uhc_glowing_countdown")
        override val codec: MapCodec<out GlowingCountdownRoutine> = MinecraftTimeDuration.CODEC.fieldOf("duration")
            .xmap(::GlowingCountdownRoutine, GlowingCountdownRoutine::duration)
    }
}