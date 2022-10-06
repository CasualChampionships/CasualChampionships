package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin {
	@Shadow public abstract double getShrinkingSpeed();

	@Shadow public abstract double getSize();

	@Shadow public abstract double getBoundWest();

	@Shadow public abstract double getBoundNorth();

	@Shadow public abstract double getBoundEast();

	@Shadow public abstract double getBoundSouth();

	@Inject(method = "clamp", at = @At("HEAD"), cancellable = true)
	private void onClamp(double x, double y, double z, CallbackInfoReturnable<BlockPos> cir) {
		double shrinkingSpeed = this.getShrinkingSpeed(); // Blocks per millisecond
		if (shrinkingSpeed > 0) {
			double margin = shrinkingSpeed * (WorldBorderManager.PORTAL_ESCAPE_TIME_SECONDS * 1000);
			margin = Math.min(margin, this.getSize() * 0.5);
			x = MathHelper.clamp(x, this.getBoundWest() + margin, this.getBoundEast() - margin);
			z = MathHelper.clamp(z, this.getBoundNorth() + margin, this.getBoundSouth() - margin);
			cir.setReturnValue(new BlockPos(x, y, z));
		}
	}
}
