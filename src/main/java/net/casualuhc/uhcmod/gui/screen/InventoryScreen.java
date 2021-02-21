package net.casualuhc.uhcmod.gui.screen;

import net.casualuhc.uhcmod.gui.button.StackButton;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

public abstract class InventoryScreen <T extends Inventory> implements AutoCloseable {
    public T inventory;

    public InventoryScreen (T inventory){
        this.inventory = inventory;
    }

    protected abstract void build();

    public T getInventory() {
        return inventory;
    }

    @Override
    public void close() {
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack == null || stack.getItem().equals(Items.AIR)) continue;
            Tag tag = stack.getOrCreateTag().get(StackButton.TAG);
            if (tag == null) continue;
            StackButton.buttons.remove(((IntTag) tag).getInt());
            inventory.removeStack(slot);
        }
    }
}
