package net.casual.championships.common.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.common.CommonMod
import net.casual.championships.common.util.CommonItems
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class BorderItem private constructor(): Item(Properties()), ArcadeModelledItem {
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
        return Items.POPPED_CHORUS_FRUIT
    }

    override fun getModeller(): ItemModeller {
        return MODELLER
    }

    companion object {
        val MODELLER = ResourcePackItemModeller(BorderItem(), CommonItems.CUSTOM_MODEL_PACK.getCreator())
        val BORDER_DISTANCE by MODELLER.model(CommonMod.id("border/border_distance"))
        val BORDER_RADIUS by MODELLER.model(CommonMod.id("border/border_radius"))
    }
}