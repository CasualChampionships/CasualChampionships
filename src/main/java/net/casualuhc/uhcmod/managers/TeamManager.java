package net.casualuhc.uhcmod.managers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.casualuhc.uhcmod.UHCMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.JsonHelper;
import net.minecraft.server.command.ServerCommandSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

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

    public static void readFile() {
        Path path = getPath();
        if (!Files.isRegularFile(path))
            return;
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            CODEC.decoder();
        }
        catch (IOException e) {
            UHCMod.UHCLogger.error("Could not read JSON :(");
        }
    }

    private static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("Teams.json");
    }

    public static void createTeams(ServerCommandSource source) {
        for (TeamManager teamManager : teamSet) {
            //code for creating team here
        }
    }


/*
"Team1": {r
   prefixrd_id: "1999199",
    name: "Scicraft",
    color: "#002000",
    prefix: "Sci",
    members: "Kerb:Santa"
}
 */



}
