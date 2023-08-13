package net.casualuhc.uhcmod.util

import net.casualuhc.uhcmod.UHCMod
import net.minecraft.resources.ResourceLocation

object ResourceUtils {
    fun id(name: String): ResourceLocation {
        return ResourceLocation(UHCMod.ID, name)
    }
}