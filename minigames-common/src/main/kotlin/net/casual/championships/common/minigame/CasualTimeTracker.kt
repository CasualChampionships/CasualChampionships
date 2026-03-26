package net.casual.championships.common.minigame

import kotlin.time.Clock
import kotlin.time.Instant

class CasualTimeTracker {
    private var start: Instant = Clock.System.now()
    private var end: Instant? = null

    fun markStart() {
        this.start = Clock.System.now()
        this.end = null
    }

    fun markEnd() {
        this.end = Clock.System.now()
    }

    fun getStart(): Instant {
        return this.start
    }

    fun getEnd(): Instant {
        if (this.end == null) {
            this.end = Clock.System.now()
        }
        return this.end!!
    }
}