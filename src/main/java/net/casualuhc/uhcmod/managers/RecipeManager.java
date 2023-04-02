package net.casualuhc.uhcmod.managers;

import net.casualuhc.arcade.recipes.RecipeFactory;
import net.casualuhc.arcade.recipes.RecipeGrid;
import net.casualuhc.arcade.recipes.RecipeHandler;
import net.casualuhc.uhcmod.utils.uhc.ItemUtils;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.util.Identifier;

public class RecipeManager {
	public static void registerRecipes() {
		RecipeHandler.register(RecipeFactory.INSTANCE.createCrafting(
			new Identifier("uhc", "golden_head_recipe"),
			CraftingRecipeCategory.MISC,
			new RecipeGrid(3, 3)
				.next(Items.GOLD_INGOT).next(Items.GOLD_INGOT).next(Items.GOLD_INGOT)
				.next(Items.GOLD_INGOT).next(Items.PLAYER_HEAD).next(Items.GOLD_INGOT)
				.next(Items.GOLD_INGOT).next(Items.GOLD_INGOT).next(Items.GOLD_INGOT),
			inventory -> ItemUtils.generateGoldenHead()
		));
		RecipeHandler.register(RecipeFactory.INSTANCE.createCrafting(
			new Identifier("uhc", "player_head_recipe"),
			CraftingRecipeCategory.MISC,
			new RecipeGrid(3, 3)
				.next(Items.ROTTEN_FLESH).next(Items.ROTTEN_FLESH).next(Items.ROTTEN_FLESH)
				.next(Items.ROTTEN_FLESH).next(Items.RABBIT_FOOT).next(Items.ROTTEN_FLESH)
				.next(Items.ROTTEN_FLESH).next(Items.ROTTEN_FLESH).next(Items.ROTTEN_FLESH),
			inventory -> ItemUtils.generateCraftedHead()
		));
	}
}
