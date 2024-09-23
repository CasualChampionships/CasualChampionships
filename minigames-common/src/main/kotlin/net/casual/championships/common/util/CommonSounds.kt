package net.casual.championships.common.util

import net.casual.arcade.resources.sound.SoundResources
import net.casual.championships.common.CommonMod

object CommonSounds: SoundResources(CommonMod.MOD_ID) {
    val COUNTDOWN_TICK_NORMAL = sound(at("countdown.tick.normal"), 0.7F)
    val COUNTDOWN_TICK_HIGH = sound(at("countdown.tick.high"), 0.7F)
    val COUNTDOWN_TICK_END = sound(at("countdown.tick.end"), 0.7F)
    val GAME_BORDER_MOVING = sound(at("game.border.moving"))
    val GAME_GRACE_END = sound(at("game.grace.end"))
    val GAME_PAUSED = sound(at("game.pause.alert"))
    val TEAM_ELIMINATION = sound(at("game.team.elimination"))
    val GLOBAL_SERVER_NOTIFICATION = sound(at("global.server.notification"))
    val GLOBAL_SERVER_NOTIFICATION_LOW = sound(at("global.server.notification.low"))

    val GAME_WON = sound(at("music.game_won"), stream = true)

    val WAITING = sound(at("music.waiting"), 1.5F, stream = true)

    fun noop() {

    }
}