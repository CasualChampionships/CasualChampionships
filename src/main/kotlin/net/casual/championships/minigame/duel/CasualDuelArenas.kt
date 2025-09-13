package net.casual.championships.minigame.duel

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.with
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.duel.arena.DuelArenasDataModule
import net.casual.championships.events.CasualConfigReloadedEvent
import net.minecraft.server.MinecraftServer

object CasualDuelArenas {
    private val path = CasualUtils.resolve("duel_arenas")

    private var arenas: DuelArenasDataModule? = null

    fun with(modules: MinigameDataModules): MinigameDataModules {
        val arenas = this.arenas ?: return modules
        return modules.with(arenas)
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerStartEvent>(priority = -1_000) { (server) ->
            this.load(server)
        }
        GlobalEventHandler.Server.register<CasualConfigReloadedEvent>(priority = -1_000) { (server) ->
            this.load(server)
        }
    }

    private fun load(server: MinecraftServer) {
        try {
            val archive = ReadableArchive.from(this.path)
            this.arenas = DuelArenasDataModule.get(archive, server)
        } catch (exception: Exception) {
            CasualUtils.logger.error("Failed to load duel arenas!", exception)
        }
    }
}