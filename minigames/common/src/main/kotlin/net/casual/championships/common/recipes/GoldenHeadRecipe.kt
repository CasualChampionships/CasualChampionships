package net.casual.championships.common.recipes

import net.casual.arcade.recipes.CraftingRecipeBuilder
import net.casual.championships.common.CasualCommonMod
import net.casual.championships.common.item.CasualCommonItems
import net.casual.championships.common.util.HeadUtils
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.ShapedRecipe

object GoldenHeadRecipe {
    fun create(): RecipeHolder<ShapedRecipe> {
        return CraftingRecipeBuilder.shaped {
            id = CasualCommonMod.id("golden_head_recipe")
            category = CraftingBookCategory.MISC
            height = 3
            width = 3
            result = HeadUtils.createConsumableGoldenHead()
            val x = Items.GOLD_INGOT
            val o = CasualCommonItems.PLAYER_HEAD
            ingredients(
                x, x, x,
                x, o, x,
                x, x, x
            )
        }.build()
    }
}