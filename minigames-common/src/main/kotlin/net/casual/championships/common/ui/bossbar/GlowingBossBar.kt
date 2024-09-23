package net.casual.championships.common.ui.bossbar

import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.TimeUtils.formatMMSS
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.casual.championships.common.util.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class GlowingBossBar: TimerBossbar() {
    override fun getTitle(player: ServerPlayer): Component {
        return CommonComponents.GLOWING_BACKGROUNDED.generate(this.getRemainingDuration().formatMMSS())
    }

    override fun getProgress(player: ServerPlayer): Float {
        return MathUtils.centeredScale(super.getProgress(player), 0.75F)
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.GREEN
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return BossEvent.BossBarOverlay.PROGRESS
    }
}