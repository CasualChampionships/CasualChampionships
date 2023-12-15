package net.casual.championships.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.championships.database.JsonUHCDatabase
import net.casual.championships.database.MongoUHCDataBase
import net.casual.championships.database.UHCDataBase
import net.casual.championships.events.uhc.CasualConfigReloaded
import net.casual.championships.util.Config

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
        val mongo = mongo
        if (mongo === null) {
            if (!DataManager::database.isInitialized) {
                database = JsonUHCDatabase()
            }
            return
        }
        val type = if (Config.dev) TESTING else PRODUCTION
        database = MongoUHCDataBase(type, mongo, LAST, COMBINED, TEAMS)
    }
}
