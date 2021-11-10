package net.casualuhc.uhcmod.mixin;


import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Inject(at = @At("HEAD"), method = "canPlayerModifyAt", cancellable = true)
    public void canPlayerModifyAt(PlayerEntity player, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(!Objects.requireNonNull(player.getEntityWorld().getServer()).isSpawnProtected(player.getEntityWorld().getServer().getWorld(player.getEntityWorld().getRegistryKey()), pos, player) && Math.abs(pos.getX()) < 30000000 && Math.abs(pos.getZ()) < 30000000);
    }

    @Inject(at = @At("HEAD"), method = "tickEntity", cancellable = true)
    public void tickEntity(Entity entity, CallbackInfo ci) {
        //Checking to see if the player is on the server side and the entity is a player
        if (entity instanceof ServerPlayerEntity player) {
            PlayerUtils.updateActionBar(player);
            PlayerUtils.updateWorldBorderArrow(player);
        }
    }
}