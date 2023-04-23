package net.casualuhc.uhcmod.uhc

object UHCEvents {
    private val events = HashMap<String, () -> UHCEvent>()

    fun getUHC(name: String): UHCEvent? {
        return this.events[name]?.invoke()
    }

    fun register(name: String, factory: () -> UHCEvent) {
        this.events[name] = factory
    }
}