package net.casual.championships.util

import net.casual.arcade.config.CustomisableConfig
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.championships.config.Event
import net.casual.championships.config.EventSerializer
import net.casual.championships.events.uhc.CasualConfigReloaded
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path

private val root = FabricLoader.getInstance().configDir.resolve("CasualUHC")

object Config: CustomisableConfig(root.resolve("config.json")) {
    private var eventNullable: Event? = null
    val dev by this.boolean("dev")

    // TODO: Cleanup this mess
    val event: Event
        get() {
            if (this.eventNullable == null) {
                return EventSerializer.deserialize(this.getObject("event")).also {
                    this.eventNullable = it
                }
            }
            return this.eventNullable!!
        }

    fun resolve(next: String): Path {
        return root.resolve(next)
    }

    fun reload() {
        this.read()
        this.eventNullable = EventSerializer.deserialize(this.getObject("event"))
        GlobalEventHandler.broadcast(CasualConfigReloaded())
    }

    internal fun registerEvents() {
        this.read()
        GlobalEventHandler.register<ServerSaveEvent> {
            this.write()
        }
    }
}
