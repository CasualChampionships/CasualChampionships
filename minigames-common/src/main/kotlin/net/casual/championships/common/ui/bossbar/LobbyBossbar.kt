package net.casual.championships.common.ui.bossbar

import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.arcade.virtual.visuals.bossbar.DynamicVirtualBossbar
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement
import net.casual.championships.common.util.CasualComponents
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent

object LobbyBossbar {
    fun create(
        server: MinecraftServer,
        timer: TimerElement,
    ): DynamicVirtualBossbar {
        val bossbar = DynamicVirtualBossbar(server)
        bossbar.addTickable(timer)
        bossbar.setTitle(UniversalElement title@ {
            if (timer.complete || !timer.hasDuration) {
                return@title CasualComponents.STARTING_SOON_BACKGROUNDED.generate().withMiniFont()
            }
            val remaining = timer.getRemainingDuration().formatHHMMSS()
            return@title CasualComponents.STARTING_IN_BACKGROUNDED.generate(remaining).withMiniFont()
        }.cached())
        bossbar.setProgress(timer.progress())
        bossbar.color.set(BossEvent.BossBarColor.GREEN)
        bossbar.overlay.set(BossEvent.BossBarOverlay.PROGRESS)
        return bossbar
    }
}