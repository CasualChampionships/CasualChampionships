package net.casual.championships.missilewars.items

import eu.pb4.polymer.core.api.item.PolymerItem
import net.fabricmc.fabric.api.networking.v1.context.PacketContext
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

class FlyingMachineItem(properties: Properties): Item(properties), PolymerItem {
    override fun getPolymerItem(stack: ItemStack, context: PacketContext): Item {
        return Items.POPPED_CHORUS_FRUIT
    }
}