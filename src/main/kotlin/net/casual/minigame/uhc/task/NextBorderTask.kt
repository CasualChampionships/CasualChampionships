package net.casual.minigame.uhc.task

import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.task.TaskCreationContext
import net.casual.minigame.uhc.UHCBorderSize
import net.casual.minigame.uhc.UHCMinigame

class NextBorderTask(
    val owner: UHCMinigame
): SavableTask {
    override val id = NextBorderTask.id

    override fun run() {
        this.owner.moveWorldBorders(this.owner.settings.borderStage.getNextStage())
    }

    companion object: MinigameTaskFactory<UHCMinigame> {
        override val id = "next_border_task"

        override fun create(minigame: UHCMinigame, context: TaskCreationContext): Task {
            return NextBorderTask(minigame)
        }
    }
}