package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.gui.screen.SpectatorScreen;
import net.minecraft.entity.player.PlayerInventory;
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
    protected ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

    private final PlayerInventory savedInventory = new PlayerInventory(self);
    private SpectatorScreen screen;

    @Inject(method = "setGameMode", at = @At("RETURN"))
    public void setGameMode(GameMode gameMode, CallbackInfo cb){
        if (gameMode == GameMode.SPECTATOR){
            this.savedInventory.clone(self.inventory);
            self.inventory.clear();
            screen = new SpectatorScreen(self);
        } else {
            self.inventory.clone(this.savedInventory);
            try {
                screen.close();
            }catch (Exception ignored){

            }finally {
                screen = null;
            }

        }

        this.server.getCommandManager().sendCommandTree((ServerPlayerEntity) (Object) this);
    }

}
