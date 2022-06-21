package net.casualuhc.uhcmod.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin {
	@Inject(method = "isSpawnProtected", at = @At("HEAD"), cancellable = true)
	private void isSpawnProtected(ServerWorld world, BlockPos pos, PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(false);
	}

	@Inject(method = "shouldPreviewChat", at = @At("HEAD"), cancellable = true)
	private void shouldPreviewChat(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(true);
	}
}
