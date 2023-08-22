package net.casualuhc.uhc.mixin.feature;

import net.casualuhc.uhc.settings.GameSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
	@Redirect(
		method = "findDimensionEntryPoint",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/border/WorldBorder;clampToBounds(DDD)Lnet/minecraft/core/BlockPos;"
		)
	)
	private BlockPos clampWithinDistanceOfWorldBorder(WorldBorder border, double x, double y, double z) {
		// Blocks per millisecond
		double shrinkingSpeed = border.getLerpSpeed();
		if (shrinkingSpeed <= 0) {
			// Border is static or expanding
			return border.clampToBounds(x, y, z);
		}
		double margin = shrinkingSpeed * (GameSettings.PORTAL_ESCAPE_TIME.getValue() * 1000);
		if (margin >= border.getSize() * 0.5) {
			// Border would reach size 0 within 30 seconds
			return BlockPos.containing(border.getCenterX(), y, border.getCenterZ());
		}

		x = Mth.clamp(x, border.getMinX() + margin, border.getMaxX() - margin);
		z = Mth.clamp(z, border.getMinZ() + margin, border.getMaxZ() - margin);
		return BlockPos.containing(x, y, z);
	}
}
