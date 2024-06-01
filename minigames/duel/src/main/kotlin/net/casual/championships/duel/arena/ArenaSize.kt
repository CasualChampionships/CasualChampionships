package net.casual.championships.duel.arena

import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

enum class ArenaSize(val display: ItemStack) {
    Small(Items.SMALL_AMETHYST_BUD.named("Small")),
    Medium(Items.MEDIUM_AMETHYST_BUD.named("Medium")),
    Large(Items.LARGE_AMETHYST_BUD.named("Large"))
}