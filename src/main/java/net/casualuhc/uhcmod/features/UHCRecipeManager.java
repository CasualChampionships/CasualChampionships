package net.casualuhc.uhcmod.features;

import net.minecraft.recipe.Recipe;

import java.util.Collection;
import java.util.List;

public class UHCRecipeManager {
	public static Collection<Recipe<?>> getCustomRecipes() {
		return List.of(
			new GoldenHeadRecipe()
		);
	}
}
