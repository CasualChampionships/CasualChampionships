package net.casualuhc.uhc.mixin.feature;

import net.casualuhc.uhc.managers.WorldBorderManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
	@Redirect(
		method = "createPortal",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private boolean isWithinWorldBorder(WorldBorder instance, BlockPos pos) {
		return isWithinBounds(instance, pos);
	}

	@Redirect(
		method = "method_39663",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"
		)
	)
	private static boolean isWithinBounds(WorldBorder instance, BlockPos pos) {
		// Blocks per millisecond
		double shrinkingSpeed = instance.getLerpSpeed();
		if (shrinkingSpeed <= 0) {
			// Border is static or expanding
			return instance.isWithinBounds(pos);
		}
		double margin = shrinkingSpeed * (WorldBorderManager.PORTAL_ESCAPE_TIME_SECONDS * 1000);
		margin = Math.min(margin, instance.getSize() * 0.5 - 1);
		return pos.getX() >= instance.getMinX() + margin && pos.getX() + 1 <= instance.getMaxX() - margin
			&& pos.getZ() >= instance.getMinZ() + margin && pos.getZ() + 1 <= instance.getMaxZ() - margin;
	}
}
