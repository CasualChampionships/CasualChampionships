package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.uhc.UHCUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;", shift = At.Shift.AFTER))
    private void afterDescriptionSet(CallbackInfo ci) {
        UHCUtils.setDescriptor((MinecraftServer) (Object) this);
    }

    @Redirect(
        method = "createWorlds",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/border/WorldBorder;addListener(Lnet/minecraft/world/border/WorldBorderListener;)V"
        )
    )
    private void onAddSyncListener(WorldBorder instance, WorldBorderListener listener) {

    }
}
