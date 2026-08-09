package net.casual.championships.common.ui.bossbar

import net.casual.arcade.utils.MathUtils
import net.casual.arcade.utils.TimeUtils.formatMMSS
import net.casual.arcade.virtual.visuals.bossbar.DynamicVirtualBossbar
import net.casual.arcade.virtual.visuals.elements.UniversalElement
import net.casual.arcade.virtual.visuals.utils.elements.timer.TimerElement
import net.casual.championships.common.util.CasualComponents
import net.minecraft.server.MinecraftServer
import net.minecraft.world.BossEvent

object GraceBossbar {
    fun create(
        server: MinecraftServer,
        timer: TimerElement,
    ): DynamicVirtualBossbar {
        val bossbar = DynamicVirtualBossbar(server)
        bossbar.addTickable(timer)
        bossbar.setTitle(timer.remaining { time -> CasualComponents.GRACE_BACKGROUNDED.generate(time.formatMMSS()) })
        bossbar.setProgress(UniversalElement { MathUtils.centeredScale(timer.getProgress(), 0.75F) })
        bossbar.color.set(BossEvent.BossBarColor.GREEN)
        bossbar.overlay.set(BossEvent.BossBarOverlay.PROGRESS)
        return bossbar
    }
}