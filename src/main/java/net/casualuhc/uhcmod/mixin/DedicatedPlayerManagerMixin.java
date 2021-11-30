package net.casualuhc.uhcmod.mixin;

import com.mojang.authlib.GameProfile;
import net.casualuhc.uhcmod.managers.GameManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DedicatedPlayerManager.class)
public abstract class DedicatedPlayerManagerMixin extends PlayerManager {
    public DedicatedPlayerManagerMixin(MinecraftServer server, DynamicRegistryManager.Impl registryManager, WorldSaveHandler saveHandler, int maxPlayers) {
        super(server, registryManager, saveHandler, maxPlayers);
    }

    @Inject(method = "isWhitelisted", at = @At("RETURN"), cancellable = true)
    private void onCheckWhitelist(GameProfile profile, CallbackInfoReturnable<Boolean> cir) {
        // Only OPs can join if the game is not setup
        if (!GameManager.isReadyForPlayers()) {
            cir.setReturnValue(this.getOpList().get(profile) != null);
        }
    }
}
