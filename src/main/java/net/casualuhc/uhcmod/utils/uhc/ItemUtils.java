package net.casualuhc.uhcmod.utils.uhc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemUtils {
	/**
	 * Creates an {@link ItemStack} with size 1 with a given name.
	 *
	 * @param item the item to create the stack.
	 * @param string the name to give the stack.
	 * @return the named item stack
	 */
	public static ItemStack named(Item item, String string) {
		ItemStack stack = new ItemStack(item);
		return stack.setCustomName(Text.literal(string));
	}
}
