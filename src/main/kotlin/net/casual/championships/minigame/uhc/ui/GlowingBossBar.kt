package net.casual.championships.minigame.uhc.ui

import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.utils.BossbarUtils
import net.casual.arcade.utils.TimeUtils
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.championships.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class GlowingBossBar: TimerBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        return Texts.BOSSBAR_GLOWING.generate(this.getRemainingDuration().formatHHMMSS())
    }

    override fun getProgress(player: ServerPlayer): Float {
        return BossbarUtils.shrink(super.getProgress(player), 0.75F)
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.GREEN
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return BossEvent.BossBarOverlay.PROGRESS
    }
}