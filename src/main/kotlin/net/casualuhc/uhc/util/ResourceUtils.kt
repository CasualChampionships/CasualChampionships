package net.casualuhc.uhc.util

import net.casualuhc.uhc.UHCMod
import net.minecraft.resources.ResourceLocation

object ResourceUtils {
    fun id(name: String): ResourceLocation {
        return ResourceLocation(UHCMod.ID, name)
    }
}