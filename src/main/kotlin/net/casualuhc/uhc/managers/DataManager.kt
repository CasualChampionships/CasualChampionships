package net.casualuhc.uhc.managers

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.uhc.database.EmptyUHCDataBase
import net.casualuhc.uhc.database.MongoUHCDataBase
import net.casualuhc.uhc.database.UHCDataBase
import net.casualuhc.uhc.events.uhc.UHCConfigLoadedEvent
import net.casualuhc.uhc.util.Config

object DataManager {
    private const val TESTING = "TestUHC"
    private const val PRODUCTION = "CasualUHC"
    private const val LAST = "last_player_stats"
    private const val COMBINED = "combined_player_stats"
    private const val TEAMS = "teams"

    lateinit var database: UHCDataBase
        private set

    internal fun registerEvents() {
        GlobalEventHandler.register<UHCConfigLoadedEvent> { this.onConfigLoaded() }
        GlobalEventHandler.register<ServerStoppedEvent> { this.database.shutdown() }
    }

    private fun onConfigLoaded() {
        val mongo = Config.stringOrNull("mongo")
        if (mongo === null) {
            this.database = EmptyUHCDataBase()
            return
        }
        val type = if (Config.booleanOrDefault("dev", true)) TESTING else PRODUCTION
        this.database = MongoUHCDataBase(type, mongo, LAST, COMBINED, TEAMS)
    }
}
