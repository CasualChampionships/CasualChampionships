package net.casualuhc.uhcmod.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerMixin {

    @Shadow @Final public MinecraftServer server;

    @Inject(method = "setGameMode", at = @At("RETURN"))
    public void setGameMode(GameMode gameMode, CallbackInfo cb){
        this.server.getCommandManager().sendCommandTree((ServerPlayerEntity) (Object) this);
    }

}
