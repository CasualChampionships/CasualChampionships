package net.casualuhc.uhcmod.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoatEntity.class)
public class BoatEntityMixin {
	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> ci) {
		if (source.getAttacker() instanceof ServerPlayerEntity player && player.interactionManager.getGameMode() == GameMode.ADVENTURE) {
			ci.setReturnValue(false);
		}
	}
}
