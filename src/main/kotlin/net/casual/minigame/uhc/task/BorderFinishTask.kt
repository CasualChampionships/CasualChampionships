package net.casual.minigame.uhc.task

import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.task.TaskCreationContext
import net.casual.minigame.uhc.UHCMinigame

class BorderFinishTask(
    private val uhc: UHCMinigame
): SavableTask {
    override val id = BorderFinishTask.id

    override fun run() {
        this.uhc.onBorderFinish()
    }

    companion object: MinigameTaskFactory<UHCMinigame> {
        override val id = "border_finish_task"

        override fun create(minigame: UHCMinigame, context: TaskCreationContext): Task {
            return BorderFinishTask(minigame)
        }
    }
}