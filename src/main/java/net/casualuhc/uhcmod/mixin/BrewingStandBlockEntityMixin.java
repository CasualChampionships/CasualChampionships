package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
	/**
	 * UHC does not allow for level 2 potions or instant heal
	 */
	@Redirect(method = "canCraft", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 0))
	private static boolean isValid(ItemStack itemStack) {
		return GameSettings.OP_POTIONS.getValue() ? itemStack.isEmpty() : itemStack.isEmpty() || itemStack.getItem() == Items.GLOWSTONE_DUST || itemStack.getItem() == Items.GLISTERING_MELON_SLICE;
	}
}
