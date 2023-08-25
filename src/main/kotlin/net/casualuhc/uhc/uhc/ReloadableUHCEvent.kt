package net.casualuhc.uhc.uhc

import net.casualuhc.uhc.minigame.uhc.UHCMinigame

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