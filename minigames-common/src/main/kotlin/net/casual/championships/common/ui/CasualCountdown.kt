package net.casual.championships.common.ui

import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.component.*
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.player.sendSound
import net.casual.arcade.utils.player.sendTitle
import net.casual.arcade.utils.player.setTitleAnimation
import net.casual.arcade.visuals.transition.TitledCountdown
import net.casual.championships.common.util.CasualComponents
import net.casual.championships.common.util.CasualSounds
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object CasualCountdown: TitledCountdown {
    override fun getCountdownTitle(current: Int): Component {
        return CasualComponents.STARTING_IN.generate("").withMiniFont()
    }

    override fun getCountdownSubtitle(current: Int): Component {
        val subtitle = Component.empty().append("▶ ").append(Component.literal(current.toString()).withMiniFont()).append(" ◀")
        when (current) {
            3 -> subtitle.red()
            2 -> subtitle.yellow()
            1 -> subtitle.lime()
        }
        return subtitle
    }

    override fun getCountdownSound(current: Int): Sound? {
        if (current <= 3) {
            return Sound(CasualSounds.COUNTDOWN_TICK_HIGH)
        }
        if (current <= 10) {
            return Sound(CasualSounds.COUNTDOWN_TICK_NORMAL)
        }
        return null
    }

    override fun afterTransition(players: Collection<ServerPlayer>) {
        val final = Sound(CasualSounds.COUNTDOWN_TICK_END)
        for (player in players) {
            player.setTitleAnimation()
            player.sendTitle(CasualComponents.GOOD_LUCK.gold().bold().withMiniFont(), Component.empty())
            player.sendSound(final)
        }
    }
}