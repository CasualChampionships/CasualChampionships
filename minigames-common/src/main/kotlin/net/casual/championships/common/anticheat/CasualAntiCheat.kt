package net.casual.championships.common.anticheat

import net.casual.championships.common.anticheat.fbp.FlexibleBlockPlacementDetector

internal object CasualAntiCheat {
    fun registerEvents() {
        FlexibleBlockPlacementDetector.registerEvents()
    }
}