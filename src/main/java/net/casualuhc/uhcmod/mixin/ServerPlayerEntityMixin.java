package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.utils.Event.Events;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerMixinInterface {
    @Unique
    private long time = 0;
    @Unique
    private boolean already = true;
    @Unique
    private final WorldBorder worldBorder = new WorldBorder();
    @Unique
    private Direction direction;
    @Unique
    private boolean coordsBoolean = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Events.ON_PLAYER_TICK.trigger((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathPre(DamageSource source, CallbackInfo ci) {
        // UHCMod.UHCSocketClient.send(this.getDamageTracker().getDeathMessage().getString());
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeathPost(DamageSource source, CallbackInfo ci) {
        Events.ON_PLAYER_DEATH.trigger((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void onDisconnect(CallbackInfo ci) {
        UHCDataBase.INSTANCE.updateStats((ServerPlayerEntity) (Object) this);
    }

    // Getters
    @Override
    public boolean getCoordsBoolean() {
        return this.coordsBoolean;
    }

    @Override
    public boolean getAlready() {
        return this.already;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    // Setters
    @Override
    public void setCoordsBoolean(boolean coordsBoolean) {
        this.coordsBoolean = coordsBoolean;
    }

    @Override
    public void setAlready(boolean already) {
        this.already = already;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }
}
