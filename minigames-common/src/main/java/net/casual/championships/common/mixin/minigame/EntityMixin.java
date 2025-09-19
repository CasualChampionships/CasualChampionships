package net.casual.championships.common.mixin.minigame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @ModifyExpressionValue(
        method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"
        )
    )
    private boolean canSerializeType(boolean original, Entity vehicle) {
        return original || vehicle.getType() == EntityType.PLAYER;
    }

    @Inject(
        method = "addPassenger",
        at = @At("TAIL")
    )
    private void onAddPassenger(Entity passenger, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetPassengersPacket(player));
        }
    }

    @Inject(
        method = "removePassenger",
        at = @At("TAIL")
    )
    private void onRemovePassenger(Entity passenger, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetPassengersPacket(player));
        }
    }
}
