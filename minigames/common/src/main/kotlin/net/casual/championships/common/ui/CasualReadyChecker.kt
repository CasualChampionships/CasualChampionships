package net.casual.championships.common.ui

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.lobby.ReadyChecker
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.impl.Sound
import net.casual.championships.common.util.CommonComponents
import net.casual.championships.common.util.CommonSounds
import net.casual.championships.common.util.CommonUI
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

class CasualReadyChecker(private val minigame: Minigame<*>): ReadyChecker {
    override fun getAlreadyNotReadyMessage(): Component {
        return CommonComponents.ALREADY_UNREADY.mini()
    }

    override fun getAlreadyReadyMessage(): Component {
        return CommonComponents.ALREADY_READY.mini()
    }

    override fun getIsReadyMessage(readier: Component): Component {
        return CommonComponents.IS_READY.generate(readier).mini()
    }

    override fun getNotReadyMessage(readier: Component): Component {
        return CommonComponents.NOT_READY.generate(readier).mini()
    }

    override fun getReadyMessage(ready: HiddenCommand, notReady: HiddenCommand): Component {
        return CommonComponents.READY_QUERY.mini()
            .append(" ")
            .append("[".literal().append(CommonComponents.YES).append("]").function(command = ready).lime())
            .append(" ")
            .append("[".literal().append(CommonComponents.NO).append("]").function(command = notReady).red())
    }

    override fun broadcast(message: Component) {
        this.minigame.chat.broadcast(message)
    }

    override fun broadcastTo(message: Component, player: ServerPlayer) {
        this.minigame.chat.broadcastTo(message, player, CommonUI.READY_ANNOUNCEMENT)
        player.sendSound(Sound(CommonSounds.GLOBAL_SERVER_NOTIFICATION))
    }
}