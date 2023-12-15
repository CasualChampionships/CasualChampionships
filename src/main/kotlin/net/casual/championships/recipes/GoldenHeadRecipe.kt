package net.casual.championships.recipes

import net.casual.arcade.recipes.CraftingRecipeBuilder
import net.casual.championships.items.CasualItems
import net.casual.championships.util.HeadUtils
import net.casual.championships.util.CasualUtils.id
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