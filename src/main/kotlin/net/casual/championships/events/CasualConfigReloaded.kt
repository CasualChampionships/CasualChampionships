package net.casual.championships.events

import net.casual.arcade.events.core.Event
import net.casual.championships.util.CasualConfig

data class CasualConfigReloaded(val config: CasualConfig): Event