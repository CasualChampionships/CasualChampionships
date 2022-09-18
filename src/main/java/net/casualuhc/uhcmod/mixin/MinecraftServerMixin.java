package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.UHCMessageDecorator;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.Event.EventHandler;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void serverLoaded(CallbackInfo ci) {
        UHCMod.onServerStart((MinecraftServer) (Object) this);
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerMetadata;setDescription(Lnet/minecraft/text/Text;)V", shift = At.Shift.AFTER))
    private void afterDescriptionSet(CallbackInfo ci) {
        GameManager.setDescriptor((MinecraftServer) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        EventHandler.onServerTick((MinecraftServer) (Object) this);
    }

    @Inject(method = "shutdown", at = @At("TAIL"))
    private void onShutdown(CallbackInfo ci) {
        UHCMod.LOGGER.info("Stopping threads...");
        UHCDataBase.shutdown();
    }

    @Inject(method = "getMessageDecorator", at = @At("HEAD"), cancellable = true)
    private void getMessageDecorator(CallbackInfoReturnable<MessageDecorator> cir) {
        cir.setReturnValue(UHCMessageDecorator.INSTANCE);
    }
}
