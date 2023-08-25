package net.casual.minigame.uhc.gui

import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.TimeUtils
import net.casual.minigame.uhc.UHCMinigame
import net.casual.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.BossEvent

class LobbyBossBar(
    val owner: UHCMinigame
): CustomBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        val time = when (this.owner.startTime) {
            Long.MAX_VALUE -> "99:99:99"
            else -> {
                val secondsLeft = (this.owner.startTime - System.currentTimeMillis()) / 1_000
                if (secondsLeft < 0) "00:00:00" else TimeUtils.formatHHMMSS(secondsLeft,
                    MinecraftTimeUnit.Seconds
                )
            }
        }
        return Texts.BOSSBAR_STARTING.generate(time)
    }

    override fun getProgress(player: ServerPlayer): Float {
        val millisLeft = this.owner.startTime - System.currentTimeMillis()
        val percentLeft = millisLeft / (30 * 60 * 1000).toFloat()
        return Mth.clamp(1 - percentLeft, 0.0F, 1.0F)
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.YELLOW
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return BossEvent.BossBarOverlay.PROGRESS
    }
}