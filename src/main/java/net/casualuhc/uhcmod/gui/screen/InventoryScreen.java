package net.casualuhc.uhcmod.gui.screen;

import net.casualuhc.uhcmod.gui.button.StackButton;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

public abstract class InventoryScreen <T extends Inventory> implements AutoCloseable {
    public T inventory;

    public InventoryScreen (T inventory){
        this.inventory = inventory;
        this.build();
    }

    protected abstract void build();

    public T getInventory() {
        return inventory;
    }

    @Override
    public void close() throws Exception {
        for (int slot = 0; slot < inventory.size(); slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack == null) continue;
            if (!stack.getOrCreateTag().contains(StackButton.TAG)) continue;
            IntTag tag = (IntTag) (Tag) stack.getSubTag(StackButton.TAG);
            assert tag != null;
            StackButton.buttons.remove(tag.getInt());
            inventory.removeStack(slot);
        }
    }
}
