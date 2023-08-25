package net.casual.util

import net.casual.CasualMod
import net.minecraft.resources.ResourceLocation

object ResourceUtils {
    fun id(name: String): ResourceLocation {
        return ResourceLocation(CasualMod.ID, name)
    }
}