package net.casual.championships.common.util

import net.casual.arcade.resources.sound.SoundResources
import net.casual.championships.common.CommonMod

object CommonSounds: SoundResources(CommonMod.MOD_ID) {
    val COUNTDOWN_TICK_NORMAL = sound(at("countdown.tick.normal"), 0.7F)
    val COUNTDOWN_TICK_HIGH = sound(at("countdown.tick.high"), 0.7F)
    val COUNTDOWN_TICK_END = sound(at("countdown.tick.end"), 0.7F)
    val GAME_BORDER_MOVING = sound(at("game.border.moving"))
    val GAME_GRACE_END = sound(at("game.grace.end"))
    val GLOBAL_SERVER_NOTIFICATION = sound(at("global.server.notification"))
    val GLOBAL_SERVER_NOTIFICATION_LOW = sound(at("global.server.notification.low"))

    fun noop() {

    }
}