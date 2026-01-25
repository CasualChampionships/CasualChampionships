package net.casual.championships.minigame.duel

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerStartEvent
import net.casual.arcade.minigame.data.MinigameDataModules
import net.casual.arcade.minigame.data.MinigameDataModules.Companion.with
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.duel.kit.DuelKitsDataModule
import net.minecraft.server.MinecraftServer

object DuelKits {
    private val path = CasualUtils.resolve("duel-kits")

    private var kits: DuelKitsDataModule? = null

    fun with(modules: MinigameDataModules): MinigameDataModules {
        val arenas = this.kits ?: return modules
        return modules.with(arenas)
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerStartEvent>(priority = -1_000) { (server) ->
            this.reload(server)
        }
    }

    internal fun reload(server: MinecraftServer) {
        try {
            val archive = ReadableArchive.from(this.path)
            this.kits = DuelKitsDataModule.get(archive, server)
        } catch (exception: Exception) {
            CasualUtils.logger.error("Failed to load duel kits!", exception)
        }
    }
}