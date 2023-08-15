package net.casualuhc.uhc.uhc

abstract class ReloadableUHCEvent: UHCEvent {
    private var loaded = false

    abstract fun onLoad()

    abstract fun onReload()

    final override fun initialise() {
        if (this.loaded) {
            this.onReload()
        } else {
            this.onLoad()
            this.loaded = true
        }
    }
}