package net.casualuhc.uhcmod.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProperties;isHardcore()Z"))
    private boolean isHardcore(WorldProperties instance) {
        return true;
    }

    @Inject(method = "checkCanJoin", at = @At("HEAD"))
    private void onCheckJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {

    }
}
