package net.casualuhc.uhcmod.util

import eu.pb4.polymer.resourcepack.api.PolymerModelData
import net.casualuhc.arcade.items.ModelledItemStates
import net.casualuhc.arcade.utils.ItemUtils.putIntElement
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

object ItemModelUtils {
    const val ID = "uhc_custom_model"

    fun ModelledItemStates.create(key: String): ResourceLocation {
        return this.createModel(ResourceUtils.id(key))
    }

    fun ItemStack.addUHCModel(id: PolymerModelData): ItemStack {
        this.putIntElement(ID, id.value())
        return this
    }

    fun ItemStack.getUHCModel(): Int {
        val tag = this.tag ?: return -1
        if (tag.contains(ID, Tag.TAG_ANY_NUMERIC.toInt())) {
            return tag.getInt(ID)
        }
        return -1
    }
}