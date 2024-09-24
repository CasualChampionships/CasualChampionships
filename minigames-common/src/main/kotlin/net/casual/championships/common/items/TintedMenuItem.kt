package net.casual.championships.common.items

import net.casual.arcade.items.ArcadeModelledItem
import net.casual.arcade.items.ItemModeller
import net.casual.arcade.items.ResourcePackItemModeller
import net.casual.championships.common.CommonMod
import net.casual.championships.common.CommonMod.id
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents.DYED_COLOR
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.DyedItemColor

class TintedMenuItem private constructor(): Item(Properties()), ArcadeModelledItem {
    override fun getPolymerItem(stack: ItemStack, player: ServerPlayer?): Item {
        return Items.WOLF_ARMOR
    }

    override fun getModeller(): ItemModeller {
        return MODELLER
    }

    override fun getPolymerItemStack(
        stack: ItemStack,
        flag: TooltipFlag,
        lookup: HolderLookup.Provider,
        player: ServerPlayer?
    ): ItemStack {
        val copy = super.getPolymerItemStack(stack, flag, lookup, player)
        copy.set(DYED_COLOR, DyedItemColor(DyedItemColor.getOrDefault(stack, 0xFFFFFF), false))
        return copy
    }

    companion object {
        val MODELLER = ResourcePackItemModeller(TintedMenuItem(), CommonMod.CUSTOM_MODEL_PACK.getCreator())
        val FLAG by MODELLER.model(id("gui/flag"))
    }
}