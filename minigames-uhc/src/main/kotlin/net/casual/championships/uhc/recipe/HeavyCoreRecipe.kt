package net.casual.championships.uhc.recipe

import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.championships.common.items.CasualItems
import net.casual.championships.uhc.CasualUHC
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory

object HeavyCoreRecipe {
    val INSTANCE = CraftingRecipeBuilder.shaped {
        key(CasualUHC.id("heavy_core"))
        category = CraftingBookCategory.MISC
        height = 3
        width = 3
        result = ItemStack(Items.HEAVY_CORE)
        val x = Items.IRON_BLOCK
        val o = CasualItems.PLAYER_HEAD
        ingredients(
            x, x, x,
            x, o, x,
            x, x, x
        )
    }
}