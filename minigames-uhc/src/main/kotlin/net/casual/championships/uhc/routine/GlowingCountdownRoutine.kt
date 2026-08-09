package net.casual.championships.uhc.routine

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.task.routine.minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.virtual.visuals.bossbar.VirtualBossbar
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement
import net.casual.championships.common.routine.BossbarCountdownRoutine
import net.casual.championships.common.ui.bossbar.GlowingBossBar
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.UHCMinigame
import net.minecraft.resources.Identifier

class GlowingCountdownRoutine(
    duration: MinecraftTimeDuration
): BossbarCountdownRoutine<UHCMinigame>(duration) {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override fun createBossbar(
        minigame: UHCMinigame,
        timer: TimerElement
    ): VirtualBossbar {
        return GlowingBossBar.create(minigame.server, timer)
    }

    override suspend fun RoutineScope<UHCMinigame>.afterCountdown() {
        minigame.weAreInTheEndgameNow()
    }

    companion object: CodecProvider<GlowingCountdownRoutine> {
        override val id: Identifier = casual("uhc_glowing_countdown")
        override val codec: MapCodec<out GlowingCountdownRoutine> = codec(::GlowingCountdownRoutine)
    }
}