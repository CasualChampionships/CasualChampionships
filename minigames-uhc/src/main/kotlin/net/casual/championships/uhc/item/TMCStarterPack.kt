package net.casual.championships.uhc.item

import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemContainerContents

object TMCStarterPack {
    fun create(): ItemStack {
        val shulker = ItemStack(Items.DYED_SHULKER_BOX.red)
        shulker.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.contents()))
        shulker.named("TMC Starter Pack", italicized = true)
        return shulker
    }

    private fun contents(): List<ItemStack> {
        return listOf(
            ItemStack(Items.PISTON, 64),
            ItemStack(Items.REDSTONE_BLOCK, 64),
            ItemStack(Items.REDSTONE_TORCH, 64),
            ItemStack(Items.DISPENSER, 64),
            ItemStack(Items.TARGET, 64),
            ItemStack(Items.CONCRETE.white, 64),
            ItemStack(Items.GLAZED_TERRACOTTA.white, 64),
            ItemStack(Items.IRON_TRAPDOOR, 64),
            ItemStack(Items.TRIPWIRE_HOOK, 64),
            ItemStack(Items.STICKY_PISTON, 64),
            ItemStack(Items.OBSERVER, 64),
            ItemStack(Items.COMPARATOR, 64),
            ItemStack(Items.DROPPER, 64),
            ItemStack(Items.NOTE_BLOCK, 64),
            ItemStack(Items.SMOOTH_STONE_SLAB, 64),
            ItemStack(Items.BRAIN_CORAL_FAN, 64),
            ItemStack(Items.STONE_PRESSURE_PLATE, 64),
            ItemStack(Items.STRING, 64),
            ItemStack(Items.SLIME_BLOCK, 64),
            ItemStack(Items.HONEY_BLOCK, 64),
            ItemStack(Items.REPEATER, 64),
            ItemStack(Items.TRAPPED_CHEST, 64),
            ItemStack(Items.COPPER_BULB.waxed.unaffected, 64),
            ItemStack(Items.DETECTOR_RAIL, 64),
            ItemStack(Items.POWERED_RAIL, 64),
            ItemStack(Items.LEVER, 64),
            ItemStack(Items.SCULK_SENSOR, 64)
        )
    }
}