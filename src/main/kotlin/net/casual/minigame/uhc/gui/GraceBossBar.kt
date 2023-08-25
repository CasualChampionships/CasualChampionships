package net.casual.minigame.uhc.gui

import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.TimeUtils
import net.casual.minigame.uhc.UHCMinigame
import net.casual.util.BossbarUtils
import net.casual.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class GraceBossBar(
    val owner: UHCMinigame,
    val end: Int
): CustomBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        val remaining = this.end - this.owner.uptime
        return Texts.BOSSBAR_GRACE.generate(TimeUtils.formatMMSS(remaining, MinecraftTimeUnit.Ticks))
    }

    override fun getProgress(player: ServerPlayer): Float {
        val percent = (this.end - this.owner.uptime) / MinecraftTimeUnit.Minutes.toTicks(10).toFloat()
        return BossbarUtils.shrink(0.0F.coerceAtLeast(percent), 0.75F)
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.GREEN
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return BossEvent.BossBarOverlay.PROGRESS
    }
}