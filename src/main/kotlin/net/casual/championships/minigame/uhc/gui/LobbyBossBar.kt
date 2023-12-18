package net.casual.championships.minigame.uhc.gui

import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.utils.TimeUtils
import net.casual.championships.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class LobbyBossBar: TimerBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        if (this.complete || !this.hasDuration) {
            return Texts.BOSSBAR_STARTING_SOON.generate()
        }
        val time = TimeUtils.formatHHMMSS(this.getRemainingDuration())
        return Texts.BOSSBAR_STARTING.generate(time)
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.YELLOW
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return BossEvent.BossBarOverlay.PROGRESS
    }
}