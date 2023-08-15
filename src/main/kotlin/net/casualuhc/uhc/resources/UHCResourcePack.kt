package net.casualuhc.uhc.resources

import eu.pb4.polymer.resourcepack.api.PolymerModelData
import eu.pb4.polymer.resourcepack.api.ResourcePackCreator
import net.casualuhc.uhc.UHCMod
import net.casualuhc.uhc.util.Config
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes

object UHCResourcePack {
    private val path = Config.resolve("packs").resolve("uhc-pack.zip")
    val pack = ResourcePackCreator.create()

    fun initialise() {
        this.pack.addAssetSource(UHCMod.ID)
        // We also include translations in the pack for replay mod
        this.pack.creationEvent.register { builder ->
            val lang = UHCMod.uhc.findPath("data/uhc/lang").get()
            for (path in lang.listDirectoryEntries()) {
                builder.addData("assets/uhc/lang/${path.name}", path.readBytes())
            }
        }

        this.pack.setPackDescription("Provides resources for Casual UHC")

        val path = UHCMod.uhc.findPath(UHCMod.uhc.metadata.getIconPath(64).get()).get()
        this.pack.packIcon = path.readBytes()

        if (!this.path.exists()) {
            if (this.generate()) {
                UHCMod.logger.info("Successfully built the UHC pack")
            } else {
                UHCMod.logger.error("Failed to build the UHC pack")
            }
        }
    }

    fun generate(): Boolean {
        return try {
            this.pack.build(this.path) { }
        } catch (e: Exception) {
            UHCMod.logger.error("Failed to build pack", e)
            false
        }
    }

    fun requestModel(item: Item, location: ResourceLocation): PolymerModelData {
        return this.pack.requestModel(item, location)
    }
}