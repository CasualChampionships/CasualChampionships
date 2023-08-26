package net.casual.minigame.uhc.task

import com.google.gson.JsonObject
import net.casual.arcade.gui.bossbar.BossBarTask
import net.casual.arcade.scheduler.SavableTask
import net.casual.minigame.uhc.UHCMinigame
import net.casual.minigame.uhc.gui.GlowingBossBar

class GlowingBossBarTask(
    uhc: UHCMinigame,
    private val end: Int
): BossBarTask(uhc, GlowingBossBar(uhc, end)), SavableTask {
    override val id = ID

    override fun writeData(json: JsonObject) {
        json.addProperty("end", this.end)
    }

    companion object {
        const val ID = "glowing_boss_bar_task"
    }
}