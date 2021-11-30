package net.casualuhc.uhcmod.mixin;

import net.minecraft.server.PlayerManager;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Redirect(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldProperties;isHardcore()Z"))
    private boolean isHardcore(WorldProperties instance) {
        return true;
    }
}
