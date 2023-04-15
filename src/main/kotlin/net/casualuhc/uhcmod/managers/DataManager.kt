package net.casualuhc.uhcmod.managers

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.uhcmod.database.EmptyUHCDataBase
import net.casualuhc.uhcmod.database.MongoUHCDataBase
import net.casualuhc.uhcmod.database.UHCDataBase
import net.casualuhc.uhcmod.events.uhc.UHCConfigLoadedEvent
import net.casualuhc.uhcmod.util.Config

object DataManager {
    private const val TESTING = "TestUHC"
    private const val PRODUCTION = "CasualUHC"
    private const val LAST = "last_player_stats"
    private const val COMBINED = "combined_player_stats"
    private const val TEAMS = "teams"

    lateinit var database: UHCDataBase
        private set

    internal fun registerEvents() {
        EventHandler.register<UHCConfigLoadedEvent> { this.onConfigLoaded() }
        EventHandler.register<ServerStoppedEvent> { this.database.shutdown() }
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
