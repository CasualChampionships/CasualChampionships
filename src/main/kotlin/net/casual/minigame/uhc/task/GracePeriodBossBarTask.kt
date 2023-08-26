package net.casual.minigame.uhc.task

import com.google.gson.JsonObject
import net.casual.arcade.gui.bossbar.BossBarTask
import net.casual.arcade.scheduler.SavableTask
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.gui.GraceBossBar

class GracePeriodBossBarTask(
    uhc: UHCMinigame,
    private val end: Int
): BossBarTask(uhc, GraceBossBar(uhc, end)), SavableTask {
    override val id = ID

    override fun writeData(json: JsonObject) {
        json.addProperty("end", this.end)
    }

    companion object {
        const val ID = "grace_period_boss_bar_task"
    }
}