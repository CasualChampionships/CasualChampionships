package net.casualuhc.uhc.util

import net.casualuhc.arcade.items.ModelledItemStates
import net.minecraft.resources.ResourceLocation

object ItemModelUtils {
    fun ModelledItemStates.create(key: String): ResourceLocation {
        return this.createModel(ResourceUtils.id(key))
    }
}