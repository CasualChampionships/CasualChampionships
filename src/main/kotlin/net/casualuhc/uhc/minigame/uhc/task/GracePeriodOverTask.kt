package net.casualuhc.uhc.minigame.uhc.task

import com.google.gson.JsonObject
import net.casualuhc.arcade.gui.bossbar.CustomBossBar
import net.casualuhc.arcade.scheduler.SavableTask
import net.casualuhc.uhc.minigame.uhc.UHCMinigame
import net.casualuhc.uhc.minigame.uhc.gui.GraceBossBar

class GracePeriodOverTask(
    private val owner: UHCMinigame,
    private val end: Int
): SavableTask() {
    override val id = ID

    private val bar: CustomBossBar

    init {
        this.bar = GraceBossBar(this.owner, this.end)
    }

    override fun run() {
        this.owner.onGraceOver()
    }

    override fun writeData(json: JsonObject) {
        json.addProperty("end", this.end)
    }

    companion object {
        const val ID = "grace_period_over_task"
    }
}