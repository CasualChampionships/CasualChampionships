package net.casual.championships.common.ui

import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.utils.BossbarUtils
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.arcade.utils.TimeUtils.formatMMSS
import net.casual.championships.common.util.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class GlowingBossBar: TimerBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        return CommonComponents.GLOWING_BACKGROUNDED.generate(this.getRemainingDuration().formatMMSS())
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