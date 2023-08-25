package net.casualuhc.uhc.resources

import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.arcade.resources.PathPackSupplier
import net.casualuhc.arcade.resources.ResourcePackHost
import net.casualuhc.uhc.events.uhc.UHCConfigReloadedEvent
import net.casualuhc.uhc.util.Config
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories

object UHCResourcePackHost {
    private val packs = Config.resolve("packs")
    // Note to self: if resource packs don't work press F3 + T before
    // you waste 2 hours trying to debug a non-existent issue
    private val host = ResourcePackHost(1)

    init {
        this.host.addPacks(PathPackSupplier(this.packs))
        this.packs.createDirectories()
    }

    fun getHostedPack(name: String): ResourcePackHost.HostedPack? {
        return this.host.getHostedPack(name)
    }

    internal fun reload(): CompletableFuture<Void> {
        return this.host.start()
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerStoppedEvent> { this.host.shutdown() }
        GlobalEventHandler.register<UHCConfigReloadedEvent> { this.reload() }
    }
}