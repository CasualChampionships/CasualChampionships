package net.casualuhc.uhcmod.mixin;


import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
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
        if (entity instanceof ServerPlayerEntity player) {
            PlayerUtils.updateActionBar(player);
            PlayerUtils.updateWorldBorderArrow(player);
        }
    }

    @Inject(method = "onPlayerConnected", at = @At("HEAD"))
    private void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        if (player.getScoreboardTeam() == null) {
            UHCMod.UHCServer.getScoreboard().addPlayerToTeam(player.getEntityName(), UHCMod.UHCServer.getScoreboard().getTeam("Spectator"));
            player.changeGameMode(GameMode.SPECTATOR);
        }
        if (GameManager.currentPhase.getPhaseNumber() < 5) {
            player.teleport(UHCMod.UHCServer.getOverworld(), 0, 253, 0, 0, 0);
        }
    }
}