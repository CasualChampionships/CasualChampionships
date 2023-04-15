package net.casualuhc.uhcmod.uhc

abstract class ReloadableUHCEvent: UHCEvent {
    private var loaded = false

    abstract fun onLoad()

    abstract fun onReload()

    final override fun load() {
        if (this.loaded) {
            this.onReload()
        } else {
            this.onLoad()
            this.loaded = true
        }
    }
}