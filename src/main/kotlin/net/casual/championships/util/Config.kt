package net.casual.championships.util

import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.championships.events.CasualConfigReloaded
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

private val root = FabricLoader.getInstance().configDir.resolve("CasualChampionships")

object Config: CustomisableConfig(root.resolve("config.json")) {
    val dev by this.boolean("dev")

    fun resolve(next: String): Path {
        return root.resolve(next)
    }

    fun reload() {
        this.read()
        GlobalEventHandler.broadcast(CasualConfigReloaded())
    }

    internal fun registerEvents() {
        this.read()
        GlobalEventHandler.register<ServerSaveEvent> {
            this.write()
        }
    }
}
