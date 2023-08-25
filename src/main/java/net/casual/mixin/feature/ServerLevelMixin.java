package net.casual.mixin.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(
		method = "mayInteract",
		at = @At("HEAD"),
		cancellable = true
	)
	private void canPlayerInteract(Player player, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		// Ignore world border and spawn protection
		cir.setReturnValue(Math.abs(pos.getX()) < 30000000 && Math.abs(pos.getZ()) < 30000000);
	}
}
