package net.casualuhc.uhcmod.mixin;

import com.mojang.authlib.GameProfile;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.stat.UHCStat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Unique
    private int halfHealthTicks = 0;

    @Shadow
    @Final
    public ServerPlayerInteractionManager interactionManager;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }


    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;
        if (PlayerManager.isPlayerPlayingInSurvival(thisPlayer) && this.getHealth() <= 1.0F) {
            this.halfHealthTicks++;
            if (this.halfHealthTicks == 1200) {
                PlayerManager.grantAdvancement(thisPlayer, UHCAdvancements.ON_THE_EDGE);
            }
        } else {
            this.halfHealthTicks = 0;
        }
        EventHandler.onPlayerTick(thisPlayer);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeathPost(DamageSource source, CallbackInfo ci) {
        EventHandler.onPlayerDeath((ServerPlayerEntity) (Object) this, source);
    }

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void onDisconnect(CallbackInfo ci) {
        EventHandler.onPlayerLeave((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "shouldDamagePlayer", at = @At("HEAD"), cancellable = true)
    private void shouldDamagePlayer(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!PlayerManager.isPlayerPlaying((ServerPlayerEntity) player)) {
            cir.setReturnValue(false);
        }
    }

    @Override
    protected void tickInVoid() {
        if (this.interactionManager.getGameMode() != GameMode.SPECTATOR) {
            super.tickInVoid();
        }
    }
}
