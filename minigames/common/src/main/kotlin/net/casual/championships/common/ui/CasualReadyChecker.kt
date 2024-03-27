package net.casual.championships.common.ui

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.lobby.ReadyChecker
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.impl.Sound
import net.casual.championships.common.util.CommonSounds
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class CasualReadyChecker(private val minigame: Minigame<*>): ReadyChecker {
    override fun broadcast(message: Component) {
        this.minigame.chat.broadcast(message)
    }

    override fun broadcastTo(message: Component, player: ServerPlayer) {
        this.minigame.chat.broadcastTo(message, player)
        player.sendSound(Sound(CommonSounds.GLOBAL_SERVER_NOTIFICATION))
    }
}