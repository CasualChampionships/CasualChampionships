package net.casual.championships.common.util

import eu.pb4.polymer.core.api.other.PolymerSoundEvent
import net.casual.championships.common.CasualCommonMod
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries

object CommonSounds {
    val COUNTDOWN_TICK_NORMAL = register("countdown.tick.normal")
    val COUNTDOWN_TICK_HIGH = register("countdown.tick.high")
    val COUNTDOWN_TICK_END = register("countdown.tick.end")

    fun noop() {

    }

    fun register(path: String): PolymerSoundEvent {
        val id = CasualCommonMod.id(path)
        val sound = PolymerSoundEvent.of(id, null)
        Registry.register(BuiltInRegistries.SOUND_EVENT, id, sound)
        return sound
    }
}