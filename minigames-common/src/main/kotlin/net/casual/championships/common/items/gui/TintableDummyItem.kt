package net.casual.championships.common.items.gui

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponents.DYED_COLOR
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.DyedItemColor

internal class TintableDummyItem(properties: Properties): Item(properties), PolymerItem {
    override fun getPolymerItem(stack: ItemStack, context: PacketContext): Item {
        return Items.WOLF_ARMOR
    }

    override fun getPolymerItemStack(
        stack: ItemStack,
        flag: TooltipFlag,
        context: PacketContext,
        lookup: HolderLookup.Provider
    ): ItemStack {
        val copy = super.getPolymerItemStack(stack, flag, context, lookup)
        copy.set(DYED_COLOR, DyedItemColor(DyedItemColor.getOrDefault(stack, 0xFFFFFF)))
        return copy
    }
}