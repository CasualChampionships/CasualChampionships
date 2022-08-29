package net.casualuhc.uhcmod.interfaces;

import net.minecraft.util.math.Direction;
import net.minecraft.world.border.WorldBorder;

public interface ServerPlayerMixinInterface {

    long getTime();

    void setTime(long time);

    boolean getAlready();

    void setAlready(boolean Already);

    boolean getCoordsBoolean();

    void setCoordsBoolean(boolean coordsBoolean);

    boolean getGlowingBoolean();

    void setGlowingBoolean(boolean glowingBoolean);

    void setDirection(Direction direction);

    Direction getDirection();

    WorldBorder getWorldBorder();
}