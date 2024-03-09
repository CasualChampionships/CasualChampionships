package net.casual.championships.common.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.task.AnyMinigameTaskFactory
import net.casual.arcade.minigame.task.impl.BossBarTask
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.task.serialization.TaskCreationContext
import net.casual.arcade.task.serialization.TaskWriteContext
import net.casual.arcade.utils.BossbarUtils.readData
import net.casual.championships.common.ui.GraceBossBar

class GracePeriodBossBarTask(
    minigame: Minigame<*>
): BossBarTask<GraceBossBar>(minigame, GraceBossBar()), SavableTask {
    override val id = Companion.id

    override fun writeCustomData(context: TaskWriteContext): JsonObject {
        return this.bar.writeData(context)
    }

    companion object: AnyMinigameTaskFactory {
        override val id = "grace_period_boss_bar_task"

        override fun create(minigame: Minigame<*>, context: TaskCreationContext): Task {
            return GracePeriodBossBarTask(minigame).readData(context)
        }
    }
}