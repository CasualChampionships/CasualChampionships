package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements ServerPlayerMixinInterface {

    @Unique
    private long time = 0;
    @Unique
    private boolean already = true;
    @Unique
    private final WorldBorder worldBorder = new WorldBorder();
    @Unique
    private Direction direction;
    @Unique
    private boolean coordsBoolean = false;

    // Getters

    @Override
    public boolean getCoordsBoolean(){
        return this.coordsBoolean;
    }

    @Override
    public boolean getAlready(){
        return this.already;
    }

    @Override
    public Direction getDirection(){
        return this.direction;
    }

    @Override
    public long getTime(){
        return this.time;
    }

    @Override
    public WorldBorder getWorldBorder(){
        return this.worldBorder;
    }

    // Setters

    @Override
    public void setCoordsBoolean(boolean coordsBoolean){
        this.coordsBoolean = coordsBoolean;
    }

    @Override
    public void setAlready(boolean already){
        this.already = already;
    }

    @Override
    public void setDirection(Direction direction){
        this.direction = direction;
    }

    @Override
    public void setTime(long time){
        this.time = time;
    }
}
