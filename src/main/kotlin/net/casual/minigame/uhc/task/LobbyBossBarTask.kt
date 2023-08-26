package net.casual.minigame.uhc.task

import net.casual.arcade.gui.bossbar.BossBarTask
import net.casual.arcade.scheduler.SavableTask
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.gui.LobbyBossBar

class LobbyBossBarTask(
    uhc: UHCMinigame
): BossBarTask(uhc, LobbyBossBar(uhc)), SavableTask {
    override val id = ID

    companion object {
        const val ID = "lobby_boss_bar_task"
    }
}