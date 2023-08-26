package net.casual.minigame.uhc.task

import net.casual.arcade.gui.bossbar.BossBarTask
import net.casual.arcade.scheduler.SavableTask
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.gui.ActiveBossBar

class ActiveBossBarTask(
    owner: UHCMinigame
): BossBarTask(owner, ActiveBossBar(owner)), SavableTask {
    override val id = ID

    companion object {
        const val ID = "active_boss_bar_task"
    }
}