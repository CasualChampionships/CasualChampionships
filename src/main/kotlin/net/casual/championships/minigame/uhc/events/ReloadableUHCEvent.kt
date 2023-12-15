package net.casual.championships.minigame.uhc.events

import net.casual.championships.minigame.uhc.UHCMinigame

abstract class ReloadableUHCEvent: UHCEvent {
    private var loaded = false

    abstract fun onLoad()

    abstract fun onReload()

    final override fun initialise(uhc: UHCMinigame) {
        if (this.loaded) {
            this.onReload()
        } else {
            this.onLoad()
            this.loaded = true
        }
    }
}