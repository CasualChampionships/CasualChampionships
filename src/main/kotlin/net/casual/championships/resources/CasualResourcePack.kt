package net.casual.championships.resources

import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casual.championships.CasualMod
import net.casual.championships.util.Config
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes

object CasualResourcePack {
    private val path = Config.resolve("packs").resolve("casual-pack.zip")
    val pack: ResourcePackCreator = ResourcePackCreator.create()

    fun initialise() {
        pack.addAssetSource(CasualMod.ID)
        // We also include translations in the pack for replay mod
        pack.creationEvent.register { builder ->
            val lang = CasualMod.casual.findPath("data/casual/lang").get()
            for (path in lang.listDirectoryEntries()) {
                builder.addData("assets/casual/lang/${path.name}", path.readBytes())
            }
        }

        pack.setPackDescription("Provides resources for Casual Championships")

        val path = CasualMod.casual.findPath(CasualMod.casual.metadata.getIconPath(64).get()).get()
        pack.packIcon = path.readBytes()

        if (!CasualResourcePack.path.exists()) {
            if (generate()) {
                CasualMod.logger.info("Successfully built the Casual pack")
            } else {
                CasualMod.logger.error("Failed to build the Casual pack")
            }
        }
    }

    fun generate(path: Path = this.path): Boolean {
        return try {
            pack.build(path) { }
        } catch (e: Exception) {
            CasualMod.logger.error("Failed to build pack", e)
            false
        }
    }
}