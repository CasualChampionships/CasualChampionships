package net.casual.recipes

import net.casual.arcade.recipes.ArcadeCustomRecipe
import net.casual.items.CasualItems
import net.casual.util.HeadUtils
import net.casual.util.ResourceUtils.id
import net.minecraft.core.RegistryAccess
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.level.Level

class GoldenHeadRecipe: ArcadeCustomRecipe(
    id("golden_head_recipe"),
    CraftingBookCategory.MISC
) {
    override fun assemble(container: CraftingContainer, registryAccess: RegistryAccess): ItemStack {
        return HeadUtils.createConsumableGoldenHead()
    }

    override fun canCraftInDimensions(width: Int, height: Int): Boolean {
        return width == 3 && height == 3
    }

    override fun matches(container: CraftingContainer, level: Level): Boolean {
        for (i in 0 until container.width * container.height) {
            val stack = container.getItem(i)
            if (if (i == 4) !stack.`is`(CasualItems.PLAYER_HEAD) else !stack.`is`(Items.GOLD_INGOT)) {
                return false
            }
        }
        return true
    }
}