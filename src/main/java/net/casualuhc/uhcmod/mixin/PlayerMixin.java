package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.gui.button.StackButton;
import net.casualuhc.uhcmod.gui.screen.SpectatorScreen;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class PlayerMixin implements ServerPlayerMixinInterface {

    @Shadow @Final public MinecraftServer server;
    protected ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;

    private final PlayerInventory savedInventory = new PlayerInventory(self);
    private SpectatorScreen screen;
    //Vars for WB and coords
    private long time = 0;
    private boolean already = true;
    private final WorldBorder worldBorder = new WorldBorder();
    private String direction;
    private boolean coordsBoolean = false;

    //setters
    public boolean getCoordsBoolean(){
        return this.coordsBoolean;
    }

    public boolean getAlready(){
        return this.already;
    }

    public String getDirection(){
        return this.direction;
    }

    public long getTime(){
        return this.time;
    }

    public WorldBorder getWorldBorder(){
        return this.worldBorder;
    }

    //getters

    public void setCoordsBoolean(boolean coordsBoolean){
        this.coordsBoolean = coordsBoolean;
    }

    public void setAlready(boolean already){
        this.already = already;
    }

    public void setDirection(String direction){
        this.direction = direction;
    }

    public void setTime(long time){
        this.time = time;
    }

    @Inject(method = "setGameMode", at = @At("RETURN"))
    public void setGameMode(GameMode gameMode, CallbackInfo cb){
        if (gameMode == GameMode.SPECTATOR){
            this.savedInventory.clone(self.inventory);
            self.inventory.clear();
            screen = new SpectatorScreen(self);
        } else {
            screen.close();
            self.inventory.clone(this.savedInventory);
            screen = null;
        }

        this.server.getCommandManager().sendCommandTree((ServerPlayerEntity) (Object) this);
    }

}
