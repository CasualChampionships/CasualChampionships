package net.casualuhc.uhcmod.mixin;

import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.casualuhc.uhcmod.event.ChatMode;

import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    public abstract ServerPlayerEntity getPlayer(UUID uuid);

    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(Text text, MessageType type, UUID sender, CallbackInfo ci) {
        if (type == MessageType.CHAT && sender != null) {
            ServerPlayerEntity player = getPlayer(sender);
            if (player != null && ChatMode.redirectMsg(player, text))
                ci.cancel();
        }
    }
}