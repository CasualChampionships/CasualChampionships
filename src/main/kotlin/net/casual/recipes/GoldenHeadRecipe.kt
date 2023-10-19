package net.casual.recipes

import net.casual.arcade.recipes.CraftingRecipeBuilder
import net.casual.items.CasualItems
import net.casual.util.HeadUtils
import net.casual.util.CasualUtils.id
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.ShapedRecipe

object GoldenHeadRecipe {
    fun create(): ShapedRecipe {
        return CraftingRecipeBuilder.shaped {
            id = id("golden_head_recipe")
            category = CraftingBookCategory.MISC
            height = 3
            width = 3
            result = HeadUtils.createConsumableGoldenHead()
            val x = Items.GOLD_INGOT
            val o = CasualItems.PLAYER_HEAD
            ingredients(
                x, x, x,
                x, o, x,
                x, x, x
            )
        }.build()
    }
}