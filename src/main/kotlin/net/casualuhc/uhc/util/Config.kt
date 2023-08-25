package net.casualuhc.uhc.util

import net.casualuhc.arcade.config.CustomisableConfig
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.uhc.events.uhc.UHCConfigReloadedEvent
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

private val root = FabricLoader.getInstance().configDir.resolve("CasualUHC")

object Config: CustomisableConfig(root.resolve("config.json")) {
    val dev by this.boolean("dev", false)

    fun resolve(next: String): Path {
        return root.resolve(next)
    }

    fun reload() {
        this.read()
        GlobalEventHandler.broadcast(UHCConfigReloadedEvent())
    }

    internal fun registerEvents() {
        this.read()
        GlobalEventHandler.register<ServerStoppedEvent> {
            this.write()
        }
    }
}
