package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerMixinInterface {

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


}
