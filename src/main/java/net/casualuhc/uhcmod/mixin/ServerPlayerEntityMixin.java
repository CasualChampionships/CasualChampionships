package net.casualuhc.uhcmod.mixin;

import com.mojang.authlib.GameProfile;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.networking.UHCDataBase;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Unique
    private int halfHealthTicks = 0;

    @Shadow
    @Final
    public ServerPlayerInteractionManager interactionManager;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile, @Nullable PlayerPublicKey publicKey) {
        super(world, pos, yaw, gameProfile, publicKey);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;
        if (PlayerUtils.isPlayerPlayingInSurvival(thisPlayer) && this.getHealth() <= 1.0F) {
            this.halfHealthTicks++;
            if (this.halfHealthTicks == 1200) {
                PlayerUtils.grantAdvancement(thisPlayer, UHCAdvancements.ON_THE_EDGE);
            }
        } else {
            this.halfHealthTicks = 0;
        }
        EventHandler.onPlayerTick(thisPlayer);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathPre(DamageSource source, CallbackInfo ci) {
        // UHCMod.UHCSocketClient.send(this.getDamageTracker().getDeathMessage().getString());
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeathPost(DamageSource source, CallbackInfo ci) {
        EventHandler.onPlayerDeath((ServerPlayerEntity) (Object) this, source);
    }

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void onDisconnect(CallbackInfo ci) {
        UHCDataBase.updateStats((ServerPlayerEntity) (Object) this);
    }

    @Override
    protected void tickInVoid() {
        if (this.interactionManager.getGameMode() != GameMode.SPECTATOR) {
            super.tickInVoid();
        }
    }
}
