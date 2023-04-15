package net.casualuhc.uhcmod.uhc

object UHCEvents {
    private val events = HashMap<String, UHCEvent>()

    fun getUHC(name: String): UHCEvent? {
        return this.events[name]
    }

    fun register(uhc: UHCEvent) {
        this.events[uhc.getName()] = uhc
    }
}