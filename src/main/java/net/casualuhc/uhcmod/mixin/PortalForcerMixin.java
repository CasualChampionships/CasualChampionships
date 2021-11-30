package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.UHCMod;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.PortalForcer;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.poi.PointOfInterest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {

    @Unique
    private boolean isNether;

    @Inject(method = "getPortalRect", at = @At("HEAD"))
    private void isNether(BlockPos destPos, boolean destIsNether, CallbackInfoReturnable<Optional<BlockLocating.Rectangle>> cir) {
        this.isNether = destIsNether;
    }

    /**
     * This fixes an issue where the player can teleport to a portal outside the world border
     */
    @Inject(method = "method_31119", at = @At("RETURN"), cancellable = true)
    private void onReturn(PointOfInterest pointOfInterest, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld world = this.isNether ? UHCMod.UHCServer.getWorld(World.NETHER) : UHCMod.UHCServer.getOverworld();
        if (world != null) {
            cir.setReturnValue(cir.getReturnValue() && world.getWorldBorder().contains(pointOfInterest.getPos()));
        }
    }
}
