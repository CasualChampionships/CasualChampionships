package net.casualuhc.uhcmod.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.casualuhc.uhcmod.utils.uhc.UHCUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;", shift = At.Shift.AFTER))
    private void afterDescriptionSet(CallbackInfo ci) {
        UHCUtils.setDescriptor((MinecraftServer) (Object) this);
    }

    @WrapWithCondition(
        method = "createWorlds",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/border/WorldBorder;addListener(Lnet/minecraft/world/border/WorldBorderListener;)V"
        )
    )
    private boolean onAddSyncListener(WorldBorder instance, WorldBorderListener listener) {
        return false;
    }
}
