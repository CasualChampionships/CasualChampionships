package net.casual.championships.resources

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.host.HostedPack
import net.casual.arcade.host.PackHost
import net.casual.arcade.host.PackHost.HostedPackRef
import net.casual.arcade.host.pack.DirectoryPackSupplier
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.utils.ResourcePackUtils.addPack
import net.casual.championships.CasualMod
import net.casual.championships.common.CommonMod
import net.casual.championships.events.CasualConfigReloaded
import net.casual.championships.uhc.UHCMod
import net.casual.championships.util.CasualConfig
import java.util.concurrent.CompletableFuture

object CasualResourcePackHost {
    private val packs = CasualConfig.resolve("packs")
    private val generated = this.packs.resolve("generated")

    private val host = PackHost(CasualMod.config.packHostIp, CasualMod.config.packHostPort)
    private val common = ArrayList<HostedPackRef>()

    val uhc: HostedPack by this.host(UHCMod.UHC_PACK)

    init {
        this.host.addSupplier(DirectoryPackSupplier(this.packs))
        for (creator in CommonMod.COMMON_PACKS) {
            this.common.add(this.host(creator))
        }
    }

    fun getCommonPacks(): List<HostedPack> {
        return this.common.map(HostedPackRef::value)
    }

    @Deprecated("")
    fun getHostedPack(name: String): HostedPack? {
        return this.host.getHostedPack(name)
    }

    internal fun reload(): CompletableFuture<Void> {
        return this.host.reload()
    }

    internal fun registerEvents() {
        this.host.start()

        GlobalEventHandler.register<ServerStoppingEvent> {
            this.host.stop()
        }
        GlobalEventHandler.register<CasualConfigReloaded> {
            this.reload()
        }
    }

    private fun host(creator: NamedResourcePackCreator): HostedPackRef {
        return this.host.addPack(this.generated, creator)
    }
}