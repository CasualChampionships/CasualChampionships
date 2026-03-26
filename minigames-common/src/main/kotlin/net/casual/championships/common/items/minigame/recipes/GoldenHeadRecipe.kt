package net.casual.championships.common.items.minigame.recipes

import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.util.casual
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory

object GoldenHeadRecipe {
    val INSTANCE = CraftingRecipeBuilder.shaped {
        key(casual("golden_head_recipe"))
        category = CraftingBookCategory.MISC
        height = 3
        width = 3
        result = ItemStackTemplate(CasualItems.GOLDEN_HEAD)
        val x = Items.GOLD_INGOT
        val o = CasualItems.PLAYER_HEAD
        ingredients(
            x, x, x,
            x, o, x,
            x, x, x
        )
    }
}