package net.casual.resources

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.arcade.resources.DirectoryPackSupplier
import net.casual.arcade.resources.HostedPack
import net.casual.arcade.resources.PackHost
import net.casual.events.uhc.CasualConfigReloaded
import net.casual.util.Config
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories

object CasualResourcePackHost {
    private val packs = Config.resolve("packs")
    // Note to self: if resource packs don't work, press F3 + T before
    // you waste 2 hours trying to debug a non-existent issue
    private val host = PackHost(1)

    private val ip by Config.stringOrNull("pack_host_ip")
    private val port by Config.int("pack_host_port", 24464)

    init {
        this.host.addPacks(DirectoryPackSupplier(this.packs))
        this.packs.createDirectories()
        this.reload()
    }

    fun getHostedPack(name: String): HostedPack? {
        return this.host.getHostedPack(name)
    }

    internal fun reload(): CompletableFuture<Boolean> {
        return this.host.start(this.ip, this.port)
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerStoppedEvent> { host.shutdown() }
        GlobalEventHandler.register<CasualConfigReloaded> { reload() }
    }
}