package net.casual.championships.common.items.gui

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

internal class DummyItem(properties: Properties): Item(properties), PolymerItem {
    override fun getPolymerItem(stack: ItemStack, context: PacketContext): Item {
        return Items.POPPED_CHORUS_FRUIT
    }
}