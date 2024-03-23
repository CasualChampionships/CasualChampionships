package net.casual.championships.common.ui

import com.google.gson.JsonObject
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.minigame.events.lobby.ui.CountdownConfig
import net.casual.arcade.minigame.events.lobby.ui.CountdownConfigFactory
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.impl.Sound
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonSounds
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

object CasualCountdown: TitledCountdown {
    override fun getCountdownTitle(current: Int): Component {
        // TODO: Make this translatable
        return (super.getCountdownTitle(current))
    }

    override fun getCountdownSound(current: Int): Sound {
        if (current <= 3) {
            return Sound(CommonSounds.COUNTDOWN_TICK_HIGH)
        }
        return Sound(CommonSounds.COUNTDOWN_TICK_NORMAL)
    }

    override fun afterCountdown(players: Collection<ServerPlayer>) {
        val final = Sound(CommonSounds.COUNTDOWN_TICK_END)
        for (player in players) {
            player.sendTitle(CommonComponents.GOOD_LUCK_MESSAGE.gold().bold())
            player.sendSound(final)
        }
    }

    object Config: CountdownConfig, CountdownConfigFactory {
        override val id: String = "casual_countdown"

        override fun create(): Countdown {
            return CasualCountdown
        }

        override fun create(data: JsonObject): CountdownConfig {
            return this
        }

        override fun write(): JsonObject {
            return JsonObject()
        }
    }
}