package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Redirect(method = "createPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean isWithinDistanceOfWorldBorderNonStatic(WorldBorder border, BlockPos pos) {
        return isWithinDistanceOfWorldBorder(border, pos);
    }

    @Redirect(method = "method_39663(Lnet/minecraft/world/border/WorldBorder;Lnet/minecraft/world/poi/PointOfInterest;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;contains(Lnet/minecraft/util/math/BlockPos;)Z"))
    private static boolean isWithinDistanceOfWorldBorder(WorldBorder border, BlockPos pos) {
        double shrinkingSpeed = border.getShrinkingSpeed(); // Blocks per millisecond
        if (shrinkingSpeed <= 0) {
            // Border is static or expanding
            return border.contains(pos);
        }
        double margin = shrinkingSpeed * (WorldBorderManager.PORTAL_ESCAPE_TIME_SECONDS * 1000);
        margin = Math.min(margin, border.getSize() * 0.5 - 1);
        return pos.getX() >= border.getBoundWest() + margin && pos.getX() + 1 <= border.getBoundEast() - margin
            && pos.getZ() >= border.getBoundNorth() + margin && pos.getZ() + 1 <= border.getBoundSouth() - margin;
    }
}
