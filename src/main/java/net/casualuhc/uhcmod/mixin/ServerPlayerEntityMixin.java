package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.Event.EventHandler;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        EventHandler.onPlayerTick((ServerPlayerEntity) (Object) this);
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
}
