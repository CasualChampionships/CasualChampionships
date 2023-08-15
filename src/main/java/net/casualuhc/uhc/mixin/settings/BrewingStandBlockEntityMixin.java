package net.casualuhc.uhc.mixin.settings;

import com.llamalad7.mixinextras.sugar.Local;
import net.casualuhc.uhc.settings.GameSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public class BrewingStandBlockEntityMixin {
	@Inject(
		method = "isBrewable",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"
		),
		cancellable = true)
	private static void canBrew(
		NonNullList<ItemStack> items,
		CallbackInfoReturnable<Boolean> cir,
		@Local ItemStack stack
	) {
		if (!GameSettings.OP_POTIONS.getValue()) {
			cir.setReturnValue(stack.is(Items.GLOWSTONE_DUST) || stack.is(Items.GLISTERING_MELON_SLICE));
		}
	}
}
