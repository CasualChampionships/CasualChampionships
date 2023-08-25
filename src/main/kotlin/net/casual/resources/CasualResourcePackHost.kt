package net.casual.resources

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.arcade.resources.PathPackSupplier
import net.casual.arcade.resources.ResourcePackHost
import net.casual.events.uhc.UHCConfigReloadedEvent
import net.casual.util.Config
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories

object CasualResourcePackHost {
    private val packs = Config.resolve("packs")
    // Note to self: if resource packs don't work press F3 + T before
    // you waste 2 hours trying to debug a non-existent issue
    private val host = ResourcePackHost(1)

    init {
        host.addPacks(PathPackSupplier(packs))
        packs.createDirectories()
    }

    fun getHostedPack(name: String): ResourcePackHost.HostedPack? {
        return host.getHostedPack(name)
    }

    internal fun reload(): CompletableFuture<Void> {
        return host.start()
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerStoppedEvent> { host.shutdown() }
        GlobalEventHandler.register<UHCConfigReloadedEvent> { reload() }
    }
}