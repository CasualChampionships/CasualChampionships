package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SweetBerryBushBlock.class)
public class SweetBerryBushBlockMixin {
	@Inject(method = "onEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
	private void onEntityDamage(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		if (entity instanceof ServerPlayerEntity player) {
			PlayerUtils.grantAdvancement(player, UHCAdvancements.THATS_EMBARRASSING);
		}
	}
}
