package net.casual.championships.common.mixins.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @ModifyExpressionValue(
        method = "useWithoutItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSources;badRespawnPointExplosion(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/damagesource/DamageSource;"
        )
    )
    private DamageSource createBedDamageSource(DamageSource original, @Local(argsOnly = true) Player player) {
        DamageSourceAccessor accessor = (DamageSourceAccessor) original;
        accessor.setCausingEntity(player);
        accessor.setDirectEntity(player);
        return original;
    }
}
