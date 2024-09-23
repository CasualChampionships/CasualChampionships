package net.casual.championships.common.ui.bossbar

import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.casual.championships.common.util.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class LobbyBossbar: TimerBossbar() {
    override fun getTitle(player: ServerPlayer): Component {
        if (this.complete || !this.hasDuration) {
            return CommonComponents.STARTING_SOON_BACKGROUNDED.generate().mini()
        }
        val time = this.getRemainingDuration().formatHHMMSS()
        return CommonComponents.STARTING_IN_BACKGROUNDED.generate(time).mini()
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.YELLOW
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return BossEvent.BossBarOverlay.PROGRESS
    }
}