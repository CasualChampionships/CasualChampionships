package net.casualuhc.uhc.minigame.uhc.task

import com.google.gson.JsonObject
import net.casualuhc.arcade.gui.bossbar.CustomBossBar
import net.casualuhc.arcade.scheduler.SavableTask
import net.casualuhc.uhc.minigame.uhc.UHCMinigame
import net.casualuhc.uhc.minigame.uhc.gui.GlowingBossBar

class GlowingTask(
    private val owner: UHCMinigame,
    private val end: Int
): SavableTask() {
    override val id = ID

    private val bar: CustomBossBar

    init {
        this.bar = GlowingBossBar(this.owner, this.end)
        this.owner.addBossbar(this.bar)
    }

    override fun run() {
        this.owner.onBorderFinish()
        this.owner.removeBossbar(this.bar)
    }

    override fun writeData(json: JsonObject) {
        json.addProperty("end", this.end)
    }

    companion object {
        const val ID = "waiting_border_end_task"
    }
}