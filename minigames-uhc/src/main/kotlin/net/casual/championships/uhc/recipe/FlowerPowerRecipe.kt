package net.casual.championships.uhc.recipe

import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.recipe.CraftingRecipeBuilder
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.item.UHCItems
import net.minecraft.core.RegistryAccess
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.ShapedRecipe

object FlowerPowerRecipe {
    private lateinit var instance: RecipeHolder<ShapedRecipe>

    fun getOrCreate(access: RegistryAccess): RecipeHolder<ShapedRecipe> {
        if (!this::instance.isInitialized) {
            this.instance = CraftingRecipeBuilder.shaped(access) {
                key(casual("flower_power"))
                category = CraftingBookCategory.MISC
                height = 3
                width = 3
                result = ItemStackTemplate.fromNonEmptyStack(UHCItems.FLOWER_POWER.named("Flower Power"))
                val x = ItemTags.SMALL_FLOWERS
                val o = Items.DIAMOND_BLOCK
                ingredients(x, x, x, x)
                ingredients(o)
                ingredients(x, x, x, x)
            }
        }
        return this.instance
    }
}