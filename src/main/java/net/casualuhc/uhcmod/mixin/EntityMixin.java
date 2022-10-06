package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin {
    @Redirect(method = "getTeleportTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;clamp(DDD)Lnet/minecraft/util/math/BlockPos;"))
    private BlockPos clampWithinDistanceOfWorldBorder(WorldBorder border, double x, double y, double z) {
        double shrinkingSpeed = border.getShrinkingSpeed(); // Blocks per millisecond
        if (shrinkingSpeed <= 0) {
            // Border is static or expanding
            return border.clamp(x, y, z);
        }
        double margin = shrinkingSpeed * (WorldBorderManager.PORTAL_ESCAPE_TIME_SECONDS * 1000);
        if (margin >= border.getSize() * 0.5) {
            // Border would reach size 0 within 30 seconds
            return new BlockPos(border.getCenterX(), y, border.getCenterZ());
        }

        x = MathHelper.clamp(x, border.getBoundWest() + margin, border.getBoundEast() - margin);
        z = MathHelper.clamp(z, border.getBoundNorth() + margin, border.getBoundSouth() - margin);
        return new BlockPos(x, y, z);
    }
}
