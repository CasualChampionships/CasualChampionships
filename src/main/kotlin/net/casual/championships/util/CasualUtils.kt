package net.casual.championships.util

import net.casual.championships.CasualMod
import net.minecraft.resources.ResourceLocation

object CasualUtils {
    fun id(name: String): ResourceLocation {
        return ResourceLocation(CasualMod.ID, name)
    }
}