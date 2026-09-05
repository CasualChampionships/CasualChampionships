package net.casual.championships.common.mixins.event;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.common.event.DoesWaypointIgnoreReceiverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.waypoints.WaypointTransmitter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WaypointTransmitter.class)
public interface WaypointTransmitterMixin {
    @WrapMethod(method = "doesSourceIgnoreReceiver")
    private static boolean overrideBehavior(LivingEntity source, ServerPlayer receiver, Operation<Boolean> original) {
        var event = new DoesWaypointIgnoreReceiverEvent(source, receiver);
        GlobalEventHandler.Server.broadcast(event);
        if (event.isCancelled()) {
            return event.result();
        }
        return original.call(source, receiver);
    }
}
