package net.casual.championships.util

import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.championships.events.uhc.CasualConfigReloaded
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

private val root = FabricLoader.getInstance().configDir.resolve("CasualUHC")

object Config: CustomisableConfig(root.resolve("config.json")) {
    val operators = this.getArrayOrDefault("operators").map { it.asString }
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
        GlobalEventHandler.register<ServerStoppedEvent> {
            this.write()
        }
    }
}
