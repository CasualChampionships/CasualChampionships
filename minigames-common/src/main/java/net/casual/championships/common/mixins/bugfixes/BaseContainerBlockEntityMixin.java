package net.casual.championships.common.mixins.bugfixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.utils.player.PlayerUtilsKt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BaseContainerBlockEntity.class)
public class BaseContainerBlockEntityMixin {
    @WrapOperation(
        method = "sendChestLockedNotifications",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/Entity;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
        )
    )
    private static void disableGlobalSoundIfSpectating(
        Level instance,
        Entity entity,
        double x,
        double y,
        double z,
        SoundEvent sound,
        SoundSource source,
        float volume,
        float pitch,
        Operation<Void> original,
        @Local(argsOnly = true) Vec3 pos
    ) {
        if (entity instanceof ServerPlayer player && player.isSpectator()) {
            PlayerUtilsKt.sendSound(player, sound, source, pos, volume, pitch);
            return;
        }

        original.call(instance, entity, x, y, z, sound, source, volume, pitch);
    }
}
