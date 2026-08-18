package net.casual.championships.common.ui.bossbar

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.stats.ArcadeStats
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.arcade.utils.component.shadowless
import net.casual.arcade.virtual.visuals.bossbar.DynamicVirtualBossbar
import net.casual.championships.common.util.CasualComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.BossEvent

object ActiveBossbar {
    fun create(minigame: Minigame): DynamicVirtualBossbar {
        val bossbar = DynamicVirtualBossbar(minigame.server)

        bossbar.setTitle { player ->
            val start = Component.empty()
                .append(SpacingFontResources.spaced(-5))
                .append(CasualComponents.Hud.BACKGROUND_40.copy().shadowless())
                .append(SpacingFontResources.spaced(-27))
                .append(Component.literal("%02d".format(minigame.players.playingPlayerCount)).withMiniFont())
                .append(CasualComponents.Hud.PLAYER_COUNT)
                .append(SpacingFontResources.spaced(43))
            val end = Component.empty()
                .append(SpacingFontResources.spaced(37))
                .append(CasualComponents.Hud.BACKGROUND_40.copy().shadowless())
                .append(SpacingFontResources.spaced(-27))
                .append(Component.literal("%02d".format(minigame.stats.getOrCreateStat(player, ArcadeStats.KILLS).value)).withMiniFont())
                .append(SpacingFontResources.spaced(1))
                .append(CasualComponents.Hud.KILLS_COUNT)
            val middle = CasualComponents.TIME_ELAPSED_BACKGROUNDED.generate(minigame.uptime.Ticks.formatHHMMSS()).withMiniFont()
            start.append(middle).append(end)
        }

        bossbar.progress.set(1.0F)
        bossbar.color.set(BossEvent.BossBarColor.YELLOW)
        bossbar.overlay.set(BossEvent.BossBarOverlay.PROGRESS)
        return bossbar
    }
}