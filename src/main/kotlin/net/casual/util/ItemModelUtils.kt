package net.casual.util

import net.casual.arcade.items.ModelledItemStates
import net.minecraft.resources.ResourceLocation

object ItemModelUtils {
    fun ModelledItemStates.create(key: String): ResourceLocation {
        return this.createModel(ResourceUtils.id(key))
    }
}