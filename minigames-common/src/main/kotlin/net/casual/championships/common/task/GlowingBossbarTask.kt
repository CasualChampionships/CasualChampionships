package net.casual.championships.common.task

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.task.MinigameTaskCreationContext
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.minigame.task.impl.BossbarTask
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.utils.error.RichResult
import net.casual.championships.common.ui.bossbar.GlowingBossBar
import net.casual.championships.common.util.casual
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class GlowingBossbarTask(
    minigame: Minigame
): BossbarTask<GlowingBossBar>(minigame, GlowingBossBar()), SavableTask {
    override val id = Companion.id

    override fun serialize(output: ValueOutput, context: TaskSerializationContext) {
        return this.bar.writeData(output, context)
    }

    companion object: MinigameTaskFactory<Minigame> {
        override val id = casual("glowing_boss_bar_task")

        override fun create(input: ValueInput, context: MinigameTaskCreationContext<Minigame>): RichResult<Task> {
            val task = GlowingBossbarTask(context.minigame)
            task.readData(input, context)
            return RichResult.success(task)
        }
    }
}