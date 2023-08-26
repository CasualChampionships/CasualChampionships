package net.casual.minigame.uhc.task

import net.casual.arcade.scheduler.SavableTask
import net.casual.minigame.uhc.UHCMinigame

class NextBorderTask(
    val owner: UHCMinigame
): SavableTask() {
    override val id = ID

    override fun run() {
        this.owner.settings.borderStage = this.owner.settings.borderStage.getNextStage()
    }

    companion object {
        const val ID = "next_border_task"
    }
}