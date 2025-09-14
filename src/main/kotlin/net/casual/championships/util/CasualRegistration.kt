package net.casual.championships.util

import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.casual.championships.minigame.CasualChampionshipsTemplate

@java.lang.Deprecated
object CasualRegistration {
    fun register() {
        CasualChampionshipsTemplate.register(MinigameRegistries.MINIGAMES_EVENT)
    }
}