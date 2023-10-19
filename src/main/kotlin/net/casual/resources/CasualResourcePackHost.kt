package net.casual.resources

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.arcade.resources.DirectoryPackSupplier
import net.casual.arcade.resources.HostedPack
import net.casual.arcade.resources.PackHost
import net.casual.events.uhc.UHCConfigReloadedEvent
import net.casual.util.Config
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories

object CasualResourcePackHost {
    private val packs = Config.resolve("packs")
    // Note to self: if resource packs don't work, press F3 + T before
    // you waste 2 hours trying to debug a non-existent issue
    private val host = PackHost(1)

    private val ip by Config.stringOrNull("pack_host_ip")

    init {
        this.host.addPacks(DirectoryPackSupplier(this.packs))
        this.packs.createDirectories()
        this.reload()
    }

    fun getHostedPack(name: String): HostedPack? {
        return this.host.getHostedPack(name)
    }

    internal fun reload(): CompletableFuture<Boolean> {
        return this.host.start(this.ip)
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerStoppedEvent> { host.shutdown() }
        GlobalEventHandler.register<UHCConfigReloadedEvent> { reload() }
    }
}