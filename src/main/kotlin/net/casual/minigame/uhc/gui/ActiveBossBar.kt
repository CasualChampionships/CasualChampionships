package net.casual.minigame.uhc.gui

import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.scheduler.MinecraftTimeUnit
import net.casual.arcade.utils.PlayerUtils
import net.casual.arcade.utils.PlayerUtils.isSurvival
import net.casual.arcade.utils.TimeUtils
import net.casual.extensions.PlayerStat
import net.casual.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casual.minigame.uhc.UHCMinigame
import net.casual.util.Texts
import net.casual.util.Texts.monospaced
import net.casual.util.Texts.shadowless
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

class ActiveBossBar(
    val owner: UHCMinigame
): CustomBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        val start = Component.empty()
            .append(Texts.space(-2))
            .append(Texts.ICON_SHORT_BACKGROUND.shadowless())
            .append(Texts.space(-27))
            .append(Texts.literal("%02d".format(PlayerUtils.players().filter { it.isSurvival }.size)).monospaced())
            .append(Texts.ICON_PLAYERS)
            .append(Texts.space(42))
            .append(Texts.space(1, 2))
        val end = Component.empty()
            .append(Texts.space(39))
            .append(Texts.ICON_SHORT_BACKGROUND.shadowless())
            .append(Texts.space(-27))
            .append(Texts.literal("%02d".format(player.uhcStats[PlayerStat.Kills].toInt())).monospaced())
            .append(Texts.ICON_KILLS)
        val middle = Texts.BOSSBAR_ELAPSED.generate(TimeUtils.formatHHMMSS(this.owner.uptime, MinecraftTimeUnit.Ticks))
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