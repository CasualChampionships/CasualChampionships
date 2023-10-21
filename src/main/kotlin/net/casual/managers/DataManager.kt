package net.casual.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.database.EmptyUHCDataBase
import net.casual.database.MongoUHCDataBase
import net.casual.database.UHCDataBase
import net.casual.events.uhc.CasualConfigReloaded
import net.casual.util.Config

object DataManager {
    private const val TESTING = "TestUHC"
    private const val PRODUCTION = "CasualUHC"
    private const val LAST = "last_player_stats"
    private const val COMBINED = "combined_player_stats"
    private const val TEAMS = "teams"

    private val mongo by Config.stringOrNull()

    lateinit var database: UHCDataBase
        private set


    internal fun registerEvents() {
        GlobalEventHandler.register<CasualConfigReloaded> { onConfigLoaded() }
        GlobalEventHandler.register<ServerStoppedEvent> { database.shutdown() }
        onConfigLoaded()
    }

    private fun onConfigLoaded() {
        val mongo = this.mongo
        if (mongo === null) {
            database = EmptyUHCDataBase()
            return
        }
        val type = if (Config.dev) TESTING else PRODUCTION
        database = MongoUHCDataBase(type, mongo, LAST, COMBINED, TEAMS)
    }
}
