package net.casualuhc.uhcmod.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class ItemUtil {
	public static ItemStack named(Item item, String string) {
		ItemStack stack = new ItemStack(item);
		return stack.setCustomName(Text.literal(string));
	}
}
