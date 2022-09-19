package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends RangedWeaponItem {
	public BowItemMixin(Settings settings) {
		super(settings);
	}

	@Inject(method = "onStoppedUsing", at = @At("TAIL"))
	private void onShootArrow(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		if (user instanceof PlayerEntity playerEntity) {
			playerEntity.getItemCooldownManager().set(this, (int) (GameSettings.BOW_COOLDOWN.getValue() * 20));
		}
	}
}
