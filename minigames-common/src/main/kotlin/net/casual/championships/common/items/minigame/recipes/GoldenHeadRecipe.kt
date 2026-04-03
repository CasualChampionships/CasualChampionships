package net.casual.championships.common.items.minigame.recipes

import net.casual.arcade.utils.ItemUtils.template
import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.championships.common.items.CasualItems
import net.casual.championships.common.util.casual
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory

object GoldenHeadRecipe {
    val INSTANCE = CraftingRecipeBuilder.shaped {
        key(casual("golden_head_recipe"))
        category = CraftingBookCategory.MISC
        height = 3
        width = 3
        result = CasualItems.GOLDEN_HEAD.template()
        val x = Items.GOLD_INGOT
        val o = CasualItems.PLAYER_HEAD
        ingredients(
            x, x, x,
            x, o, x,
            x, x, x
        )
    }
}