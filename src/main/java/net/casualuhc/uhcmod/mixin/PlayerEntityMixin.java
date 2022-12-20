package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.stat.UHCStat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V"))
	private void onAttack(PlayerEntity player, Identifier stat, int amount, Entity target) {
		player.increaseStat(stat, amount);
		if (GameManager.isGameActive() && target instanceof PlayerEntity) {
			PlayerExtension.get(player).getStats().increment(UHCStat.DAMAGE_DEALT, amount / 10.0);
		}
	}

	@Redirect(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;increaseStat(Lnet/minecraft/util/Identifier;I)V", ordinal = 1))
	private void onDamage(PlayerEntity player, Identifier stat, int amount) {
		player.increaseStat(stat, amount);
		if (GameManager.isGameActive()) {
			PlayerExtension.get(player).getStats().increment(UHCStat.DAMAGE_TAKEN, amount / 10.0);
		}
	}
}
