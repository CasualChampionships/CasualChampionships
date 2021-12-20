package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Unique
	private boolean wasPlayer = false;

	@Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V", shift = At.Shift.BEFORE))
	private void onAttack(Entity target, CallbackInfo ci) {
		if (target instanceof ServerPlayerEntity) {
			this.wasPlayer = true;
		}
	}

	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V"))
	private void onAttack(PlayerEntity thisPlayer, Identifier stat, int amount) {
		if (this.wasPlayer && thisPlayer instanceof ServerPlayerEntity player && PlayerUtils.isPlayerSurvival(player) && PlayerUtils.isPlayerPlaying(player)) {
			thisPlayer.increaseStat(stat, amount);
			this.wasPlayer = false;
		}
	}
}
