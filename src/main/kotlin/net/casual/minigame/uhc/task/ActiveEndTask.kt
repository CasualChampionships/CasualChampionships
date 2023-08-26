package net.casual.minigame.uhc.task

import net.casual.arcade.scheduler.SavableTask
import net.casual.minigame.uhc.UHCMinigame

class ActiveEndTask(
    val owner: UHCMinigame
): SavableTask {
    override val id = ID

    init {
        this.owner.createActiveSidebar()
    }

    override fun run() {
        this.owner.onActiveEnd()
    }

    companion object {
        const val ID = "active_end_task"
    }
}