package net.casual.minigame.uhc.task

import net.casual.arcade.scheduler.SavableTask
import net.casual.minigame.uhc.UHCMinigame

class BorderFinishTask(
    private val uhc: UHCMinigame
): SavableTask {
    override val id = ID

    override fun run() {
        this.uhc.onBorderFinish()
    }

    companion object {
        const val ID = "border_finish_task"
    }
}