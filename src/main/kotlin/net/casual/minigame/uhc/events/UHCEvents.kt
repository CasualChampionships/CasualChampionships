package net.casual.minigame.uhc.events

object UHCEvents {
    private val events = HashMap<String, () -> UHCEvent>()

    fun getUHC(name: String): UHCEvent? {
        return events[name]?.invoke()
    }

    fun register(name: String, factory: () -> UHCEvent) {
        events[name] = factory
    }
}