package net.casual.championships.common.task

import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.task.MinigameTaskCreationContext
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.impl.Sound
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualGuiUtils.broadcastGame
import net.casual.championships.common.util.CasualSounds
import net.casual.championships.common.util.casual
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects

class GracePeriodTask(
    val minigame: Minigame
): SavableTask {
    override val id = Companion.id

    init {
        this.minigame.settings.canPvp.set(false)

        this.minigame.players.playing.forEach {
            it.addEffect(MobEffectInstance(MobEffects.RESISTANCE, 1, 1))
        }

        this.minigame.events.register<MinigameAddPlayerEvent> { context ->
            val player = context.player
            //TODO: calc time
            player.addEffect(MobEffectInstance(MobEffects.RESISTANCE, 1, 1))
        }
    }

    override fun run() {
        minigame.chat.broadcastGame(
            CasualComponents.BORDER_GRACE_OVER.red().withMiniFont(),
            sound = Sound(CasualSounds.GAME_BORDER_MOVING)
        )
        this.minigame.settings.canPvp.set(true)
    }

    companion object: MinigameTaskFactory<Minigame> {
        override val id = casual("grace_period_task")

        override fun create(context: MinigameTaskCreationContext<Minigame>): Task {
            return GracePeriodTask(context.minigame)
        }
    }
}