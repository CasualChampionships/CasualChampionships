package net.casual.championships.resources

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppedEvent
import net.casual.arcade.resources.DirectoryPackSupplier
import net.casual.arcade.resources.HostedPack
import net.casual.arcade.resources.PackHost
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.championships.events.uhc.CasualConfigReloaded
import net.casual.championships.util.Config
import java.util.concurrent.CompletableFuture
import kotlin.io.path.createDirectories

object CasualResourcePackHost {
    private val packs = Config.resolve("packs")
    // Note to self: if resource packs don't work, press F3 + T before
    // you waste 2 hours trying to debug a non-existent issue
    private val host = PackHost(1)

    init {
        host.addPacks(DirectoryPackSupplier(packs))
        packs.createDirectories()
        GlobalTickedScheduler.later {
            reload()
        }
    }

    fun getHostedPack(name: String): HostedPack? {
        return host.getHostedPack(name)
    }

    internal fun reload(): CompletableFuture<Boolean> {
        val resources = Config.event.resources
        return host.start(resources.hostIp, resources.hostPort)
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerStoppedEvent> { host.shutdown() }
        GlobalEventHandler.register<CasualConfigReloaded> { reload() }
    }
}