package net.casual.championships.minigame.duel

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.minigame.events.lobby.ReadyChecker
import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.red
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class DuelRequester(
    private val requester: ServerPlayer,
    private val players: Collection<ServerPlayer>
): ReadyChecker {
    override fun getReadyMessage(ready: HiddenCommand, notReady: HiddenCommand): Component {
        return Component.empty().apply {
            append(requester.displayName!!)
            append(" has challenged you to a duel! ")
            append("[Accept]".literal().function(command = ready).lime())
            append(" ")
            append("[Decline]".literal().function(command = notReady).red())
        }
    }

    override fun getIsReadyMessage(readier: Component): Component {
        return Component.empty().append(readier).append(" has accepted the duel!".literal().lime())
    }

    override fun getNotReadyMessage(readier: Component): Component {
        return Component.empty().append(readier).append(" has declined the duel!".literal().lime())
    }

    override fun getAlreadyReadyMessage(): Component {
        return "You have already accepted this duel!".literal().crimson()
    }

    override fun getAlreadyNotReadyMessage(): Component {
        return "You have already declined this duel!".literal().crimson()
    }

    override fun broadcast(message: Component) {
        for (player in this.players) {
            this.broadcastTo(message, player)
        }
    }

    override fun broadcastTo(message: Component, player: ServerPlayer) {
        player.sendSystemMessage(Component.empty().append("[⚔] ".literal().lime()).append(message))
    }
}