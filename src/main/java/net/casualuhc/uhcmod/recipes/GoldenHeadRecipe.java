package net.casualuhc.uhcmod.recipes;

import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class GoldenHeadRecipe extends SpecialCraftingRecipe {
	public GoldenHeadRecipe() {
		super(new Identifier("golden_head_recipe"));
	}

	@Override
	public boolean matches(CraftingInventory inventory, World world) {
		if (inventory.getWidth() != 3 || inventory.getHeight() != 3) {
			return false;
		}
		for (int i = 0; i < inventory.getWidth(); ++i) {
			for (int j = 0; j < inventory.getHeight(); ++j) {
				ItemStack itemStack = inventory.getStack(i + j * inventory.getWidth());
				if (i == 1 && j == 1 ? !itemStack.isOf(Items.PLAYER_HEAD) : !itemStack.isOf(Items.GOLD_INGOT)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack craft(CraftingInventory inventory) {
		return PlayerUtils.generateGoldenHead();
	}

	@Override
	public boolean fits(int width, int height) {
		return width >= 2 && height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null;
	}
}
