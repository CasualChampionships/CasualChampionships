package net.casualuhc.uhcmod.event;

import com.sun.org.apache.xpath.internal.operations.Mod;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChatMode {

    public enum Mode {
        ALL,
        TEAM,
        SPECTATOR;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public String asTag(){
            return String.format("uhc_chat_%s", this.toString());
        }
    }

    public static final Style DEAD_STYLE = Style.EMPTY.withItalic(true).withColor(Formatting.GRAY);
    public static final Text DEAD = new LiteralText("(dead)").fillStyle(DEAD_STYLE);

    public static final List<String> MODES = Arrays.
            stream(Mode.values()).
            map(Mode::asTag).
            collect(Collectors.toList());

    public static void setMode(ServerPlayerEntity player, Mode mode){
        player.getScoreboardTags().removeAll(MODES);
        player.getScoreboardTags().add(mode.asTag());
    }

    public static Mode getMode(ServerPlayerEntity player){
        return Arrays.
                stream(Mode.values()).
                filter(mode -> player.getScoreboardTags().contains(mode.asTag())).
                findFirst().
                orElse(Mode.ALL);
    }

    public static boolean redirectMsg(ServerPlayerEntity player,Text text) {
        Mode mode = getMode(player);
        switch (mode){
            case ALL: return false;
            case TEAM: return redirectTeam(player, text);
            case SPECTATOR: return broadcastSpectator(player, text);
        }
        return false;
    }

    private static boolean broadcastSpectator(ServerPlayerEntity entity, Text text) {
        MinecraftServer server = entity.getServer();
        assert server != null;

        if(entity.interactionManager.getGameMode() != GameMode.SPECTATOR)
            return false;

        List<ServerPlayerEntity> list = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player: list) {
            if(player.interactionManager.getGameMode() != GameMode.SPECTATOR)
                continue;

            String translation = player == entity ? "uhc.chat.type.dead.sent" : "uhc.chat.type.dead.text";
            Text message = new TranslatableText(translation, DEAD, text);
            player.sendSystemMessage(message, entity.getUuid());
        }
        return true;
    }

    private static boolean broadcastTeamSpectator(ServerPlayerEntity entity, Text text) {
        MinecraftServer server = entity.getServer();
        assert server != null;

        Team team = (Team) entity.getScoreboardTeam();
        assert team != null;

        if(entity.interactionManager.getGameMode() != GameMode.SPECTATOR)
            return false;

        List<ServerPlayerEntity> list = server.getPlayerManager().getPlayerList();

        for (ServerPlayerEntity player: list) {
            if(player.getScoreboardTeam() != team)
                continue;

            if(player.interactionManager.getGameMode() != GameMode.SPECTATOR)
                continue;

            String translation = player == entity ? "uhc.chat.type.team.sent" : "uhc.type.team.text";
            Text message = new TranslatableText(translation, team.getFormattedName(), DEAD, text);
            player.sendSystemMessage(message, entity.getUuid());
        }
        return true;
    }




    private static boolean redirectTeam (ServerPlayerEntity player, Text text){
        Team team = (Team) player.getScoreboardTeam();
        if (team == null)
            return false;

        if(broadcastTeamSpectator(player, text))
            return true;

        String msg = text.getString().replaceFirst("<.*> ", "");
        CommandManager manager = player.getServer().getCommandManager();
        manager.execute(player.getCommandSource(), String.format("/tm %s", msg));
        return true;
    }
}
