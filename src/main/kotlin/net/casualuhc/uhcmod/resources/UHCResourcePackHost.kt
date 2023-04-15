package net.casualuhc.uhcmod.resources

import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.arcade.resources.LocalResourcePackHost
import net.casualuhc.arcade.resources.ResourcePackHost
import net.casualuhc.uhcmod.events.uhc.UHCConfigLoadedEvent
import net.casualuhc.uhcmod.util.Config
import kotlin.io.path.*

object UHCResourcePackHost {
    private val packs = Config.resolve("packs")
    // Note to self: if resource packs don't work press F3 + T before
    // you waste 2 hours trying to debug a non-existent issue
    private val host = LocalResourcePackHost(this.packs, 1)

    init {
        this.packs.createDirectories()
    }

    fun getHostedPack(name: String): ResourcePackHost.HostedPack? {
        return this.host.getHostedPack(name)
    }

    internal fun registerEvents() {
        EventHandler.register<ServerStoppedEvent> { this.host.shutdown() }
        EventHandler.register<UHCConfigLoadedEvent> { this.host.start() }
    }
}