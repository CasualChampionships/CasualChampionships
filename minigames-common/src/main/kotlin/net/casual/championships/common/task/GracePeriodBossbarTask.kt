package net.casual.championships.common.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.task.AnyMinigameTaskFactory
import net.casual.arcade.minigame.task.impl.BossbarTask
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.scheduler.task.serialization.TaskWriteContext
import net.casual.championships.common.ui.bossbar.GraceBossbar

class GracePeriodBossbarTask(
    minigame: Minigame<*>
): BossbarTask<GraceBossbar>(minigame, GraceBossbar()), SavableTask {
    override val id = Companion.id

    override fun writeCustomData(context: TaskWriteContext): JsonObject {
        return this.bar.writeData(context)
    }

    companion object: AnyMinigameTaskFactory {
        override val id = "grace_period_boss_bar_task"

        override fun create(minigame: Minigame<*>, context: TaskCreationContext): Task {
            return GracePeriodBossbarTask(minigame).readData(context)
        }
    }
}