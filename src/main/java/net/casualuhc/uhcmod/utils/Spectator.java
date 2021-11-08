package net.casualuhc.uhcmod.utils;

import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class Spectator {
    private static ServerPlayerEntity cameraman;

    public static void spectate(ServerPlayerEntity from, ServerPlayerEntity player){
        if(!from.isSpectator())
            return;
        from.teleport(
                (ServerWorld) player.world,
                player.getX(), player.getY(), player.getZ(),
                player.yaw, player.pitch);
    }

    public static void spectate(ServerPlayerEntity from){
        spectate(from, cameraman);
    }

    public static void spectate(ServerPlayerEntity from, Team team){
        spectate(from, getAnyAlive(from.server, team));
    }

    public static ServerPlayerEntity getAnyAlive(MinecraftServer server, Team team){
        return server.
                getPlayerManager().
                getPlayerList().
                stream().
                filter(player -> player.isTeamPlayer(team)).
                filter(player -> !player.isSpectator())
                .findAny()
                .orElse(cameraman);
    }

    public static void setCameraman(ServerPlayerEntity player) {
        cameraman = player;
    }

    public static ServerPlayerEntity getCameraman(){
        return cameraman;
    }
}
