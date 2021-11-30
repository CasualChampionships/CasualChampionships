package net.casualuhc.uhcmod.interfaces;

import net.casualuhc.uhcmod.managers.VoteManager;
import net.minecraft.world.border.WorldBorder;

public interface ServerPlayerMixinInterface {

    long getTime();

    void setTime(long time);

    boolean getAlready();

    void setAlready(boolean Already);

    boolean getCoordsBoolean();

    void setCoordsBoolean(boolean coordsBoolean);


    void setDirection(Direction direction);

    Direction getDirection();

    WorldBorder getWorldBorder();

    VoteManager getVoteManager();

    enum Direction {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }
}