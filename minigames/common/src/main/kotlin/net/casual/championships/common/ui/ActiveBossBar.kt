package net.casual.championships.common.ui

import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.stats.ArcadeStats
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.shadowless
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.championships.common.util.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class ActiveBossBar(
    private val owner: Minigame<*>
): CustomBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        val start = Component.empty()
            .append(ComponentUtils.space(-2))
            .append(CommonComponents.BACKGROUND_40_BITMAP.shadowless())
            .append(ComponentUtils.space(-27))
            .append("%02d".format(this.owner.getPlayingPlayers().size).literal())
            .append(CommonComponents.PLAYER_COUNT_BITMAP)
            .append(ComponentUtils.space(42))
            .append(ComponentUtils.space(1, 2))
        val end = Component.empty()
            .append(ComponentUtils.space(39))
            .append(CommonComponents.BACKGROUND_40_BITMAP.shadowless())
            .append(ComponentUtils.space(-27))
            .append("%02d".format(this.owner.stats.getOrCreateStat(player, ArcadeStats.KILLS).value).literal())
            .append(CommonComponents.KILLS_COUNT_BITMAP)
        val middle = CommonComponents.TIME_ELAPSED_BACKGROUNDED.generate(this.owner.uptime.Ticks.formatHHMMSS())
        return start.append(middle).append(end)
    }

    override fun getProgress(player: ServerPlayer): Float {
        return 1.0F
    }

    override fun getColour(player: ServerPlayer): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.YELLOW
    }

    override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
        return BossEvent.BossBarOverlay.PROGRESS
    }
}