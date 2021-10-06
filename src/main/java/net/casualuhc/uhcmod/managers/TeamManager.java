package net.casualuhc.uhcmod.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.casualuhc.uhcmod.UHCMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;
import net.minecraft.server.command.ServerCommandSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TeamManager {

    public static Set<TeamManager> teamSet = new HashSet<>();

    public String name;
    public String colour;
    public String prefix;
    public String membersAsString;
    public String[] members;

    public TeamManager(String name, String colour, String prefix, String membersAsString) {
        this.name = name;
        this.colour = colour;
        this.prefix = prefix;
        this.membersAsString = membersAsString;
        this.members = membersAsString.split(":");
        teamSet.add(this);
    }

    public static final MapCodec<TeamManager> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(t -> t.name),
            Codec.STRING.fieldOf("color").forGetter(t -> t.colour),
            Codec.STRING.fieldOf("prefix").forGetter(t -> t.prefix),
            Codec.STRING.fieldOf("members").forGetter(t -> t.membersAsString)
    ).apply(instance, TeamManager::new));

    public static UnboundedMapCodec<String, TeamManager> MAP_CODEC = Codec.unboundedMap(Codec.STRING, CODEC.codec());

    public static void readFile() {
        Path path = getPath();
        if (!Files.isRegularFile(path))
            return;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            MAP_CODEC.decode(JsonOps.INSTANCE, JsonHelper.deserialize(reader));
        }
        catch (IOException e) {
            UHCMod.UHCLogger.error("Could not read JSON :(");
        }
    }

    public static void createTeams() {
        readFile();
        Scoreboard scoreboard = UHCMod.UHCServer.getScoreboard();
        scoreboard.getTeams().forEach(scoreboard::removeTeam);
        for (TeamManager teamManager : teamSet) {
            Team team = scoreboard.getTeam(teamManager.name);
            if (team == null)
                team = scoreboard.addTeam(teamManager.name);
            team.setPrefix(new LiteralText(teamManager.prefix));
            team.setColor(Formatting.byName(teamManager.colour));
            for (String memberName : teamManager.members) {
                scoreboard.addPlayerToTeam(memberName, team);
            }
        }
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("Teams.json");
    }


/*
"Team1": {
    "name": "Scicraft",
    "color": "YELLOW",
    "prefix": "[SCI] ",
    "members": "Kerbaras:PR0CESS"
  }
 */



}
