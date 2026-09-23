package net.casual.championships.uhc.minigame.phase

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.routine.MinigameRoutine
import net.casual.arcade.minigame.routine.minigame
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.utils.call
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.championships.common.routine.BossbarCountdownRoutine
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.ui.bossbar.GraceBossbar
import net.minecraft.resources.Identifier
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

class GraceRoutine: MinigameRoutine<UHCMinigame> {
    override fun codec(): MapCodec<out Routine<UHCMinigame>> {
        return codec
    }

    override suspend fun RoutineScope<UHCMinigame>.run() {
        val grace = step(MinecraftTimeDuration.CODEC) {
            minigame.settings.isChatGlobal = false
            minigame.settings.canPvp.set(false)

            minigame.boundary.startAfter(minigame.settings.borderStartDelay)

            val grace = minigame.settings.gracePeriod
            minigame.chat.broadcastGame(
                CasualComponents.BORDER_INITIAL_GRACE.generate(grace.minutes).gold().withMiniFont()
            )

            val resistanceDuration = grace.ticks / 2
            for (player in minigame.players.playing) {
                player.addEffect(MobEffectInstance(MobEffects.RESISTANCE, resistanceDuration, 0, false, false, true))
            }
            grace
        }

        call(BossbarCountdownRoutine(grace) { timer -> GraceBossbar.create(minigame.server, timer) })
    }

    companion object: CodecProvider<GraceRoutine> {
        override val id: Identifier = casual("uhc_grace")
        override val codec: MapCodec<out GraceRoutine> = MapCodec.unit(::GraceRoutine)
    }
}