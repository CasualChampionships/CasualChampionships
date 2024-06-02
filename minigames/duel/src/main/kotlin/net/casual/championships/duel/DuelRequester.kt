package net.casual.championships.duel

import net.casual.arcade.commands.hidden.HiddenCommand
import net.casual.arcade.minigame.events.lobby.ReadyChecker
import net.casual.arcade.utils.ComponentUtils
import net.casual.arcade.utils.ComponentUtils.crimson
import net.casual.arcade.utils.ComponentUtils.function
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ComponentUtils.mini
import net.casual.arcade.utils.ComponentUtils.red
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class DuelRequester(
    private val requester: ServerPlayer,
    private val players: Collection<ServerPlayer>
): ReadyChecker {
    override fun getReadyMessage(ready: HiddenCommand, notReady: HiddenCommand): Component {
        return Component.empty()
            .append(Component.translatable("casual.duel.challenge", requester.displayName))
            .append(ComponentUtils.space(4))
            .append(
                Component.literal("[")
                    .append(Component.translatable("casual.duel.challenge.accept"))
                    .append("]")
                    .function(command = ready)
                    .lime()
            )
            .append(ComponentUtils.space(4))
            .append(
                Component.literal("[")
                    .append(Component.translatable("casual.duel.challenge.decline"))
                    .append("]")
                    .function(command = notReady)
                    .red()
            ).mini()
    }

    override fun getIsReadyMessage(readier: Component): Component {
        return Component.translatable("casual.duel.accepted", readier).lime().mini()
    }

    override fun getNotReadyMessage(readier: Component): Component {
        return Component.translatable("casual.duel.declined", readier).lime().mini()
    }

    override fun getAlreadyReadyMessage(): Component {
        return Component.translatable("casual.duel.alreadyReady").crimson().mini()
    }

    override fun getAlreadyNotReadyMessage(): Component {
        return Component.translatable("casual.duel.alreadyNotReady").crimson().mini()
    }

    override fun broadcast(message: Component) {
        for (player in this.players) {
            this.broadcastTo(message, player)
        }
    }

    override fun broadcastTo(message: Component, player: ServerPlayer) {
        player.sendSystemMessage(Component.empty().append(DUEL_PREFIX).append(" ").append(message))
    }

    companion object {
        val DUEL_PREFIX = "[⚔]".literal().lime()
    }
}