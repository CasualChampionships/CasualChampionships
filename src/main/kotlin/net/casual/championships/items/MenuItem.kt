package net.casual.championships.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.resources.CasualResourcePack
import net.casual.championships.util.CasualUtils
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class MenuItem private constructor(): Item(Properties()), ArcadeModelledItem {
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
        return Items.POPPED_CHORUS_FRUIT
    }

    override fun getModeller(): ItemModeller {
        return MODELLER
    }

    companion object {
        val MODELLER = ResourcePackItemModeller(MenuItem(), CasualResourcePack.pack)
        val YES by MODELLER.model(CasualUtils.id("gui/yes"))
        val NO by MODELLER.model(CasualUtils.id("gui/no"))
    }
}