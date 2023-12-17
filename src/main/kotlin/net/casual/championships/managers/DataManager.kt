package net.casual.championships.managers

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.championships.minigame.uhc.database.MongoUHCDatabase
import net.casual.championships.events.uhc.CasualConfigReloaded
import net.casual.championships.minigame.uhc.database.UHCDatabase
import net.casual.championships.util.Config

// TODO:
object DataManager {
    private const val TESTING = "TestUHC"
    private const val PRODUCTION = "CasualUHC"
    private const val UHC = "uhcs"
    private const val TEAMS = "teams"

    private val mongo by Config.stringOrNull()

    lateinit var database: UHCDatabase
        private set


    internal fun registerEvents() {
        GlobalEventHandler.register<CasualConfigReloaded> { onConfigLoaded() }
        GlobalEventHandler.register<ServerStoppedEvent> { database.shutdown() }
        onConfigLoaded()
    }

    private fun onConfigLoaded() {
        val mongo = mongo
        if (mongo == null) {
            database = UHCDatabase()
            return
        }
        val type = if (Config.dev) TESTING else PRODUCTION
        database = MongoUHCDatabase(type, mongo, UHC, TEAMS)
    }
}
