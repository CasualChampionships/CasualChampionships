package net.casualuhc.uhcmod.managers;

import com.google.gson.*;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class TeamManager {
    private static final Set<TeamManager> teamSet = new HashSet<>();

    private final String name;
    private final String colour;
    private final String prefix;
    private final String[] members;

    private TeamManager(String name, String colour, String prefix, String[] members) {
        this.name = name;
        this.colour = colour;
        this.prefix = prefix;
        this.members = members;
        teamSet.add(this);
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static void load() throws IOException {
        teamSet.clear();
        Path path = getPath();
        File file = path.toFile();
        BufferedReader bufferedReader = null;
        if (file.exists()) {
            try {
                bufferedReader = Files.newBufferedReader(path);
                JsonArray jsonArray = GSON.fromJson(bufferedReader, JsonArray.class);
                for (JsonElement jsonElement : jsonArray) {
                    JsonObject jsonObject = JsonHelper.asObject(jsonElement, "entry");
                    teamFromJson(jsonObject);
                }
            }
            catch (JsonParseException e) {
                UHCMod.UHCLogger.error("Could not read JSON");
                e.printStackTrace();
            }
        }
        if (bufferedReader != null) {
            bufferedReader.close();
        }
    }

    private static void teamFromJson(JsonObject json) {
        if (json.has("name") && json.has("colour") && json.has("prefix") && json.has("members")) {
            String teamName = json.get("name").getAsString();
            String colour = json.get("colour").getAsString();
            String prefix = "[%s]".formatted(json.get("prefix").getAsString());
            String[] members = json.get("members").getAsString().split(":");
            new TeamManager(teamName, colour, prefix, members);
        }
    }

    public static void createTeams() {
        try {
            load();
        }
        catch (IOException e) {
            UHCMod.UHCLogger.error("Error with buffered reader");
        }
        Scoreboard scoreboard = UHCMod.UHC_SERVER.getScoreboard();
        // This MUST be done like this or will encounter ConcurrentModificationException
        final Team[] teams = scoreboard.getTeams().toArray(Team[]::new);
        for (Team team : teams) {
            scoreboard.removeTeam(team);
        }
        for (TeamManager teamManager : teamSet) {
            Team team = scoreboard.getTeam(teamManager.name);
            if (team == null) {
                team = scoreboard.addTeam(teamManager.name);
            }
            team.setPrefix(new LiteralText("%s ".formatted(teamManager.prefix)));
            Formatting formatting = Formatting.byName(teamManager.colour);
            if (formatting != null) {
                team.setColor(formatting);
            }
            for (String memberName : teamManager.members) {
                scoreboard.addPlayerToTeam(memberName, team);
            }
            team.setFriendlyFireAllowed(false);
        }
        TeamUtils.clearNonTeam();
        Team team = scoreboard.addTeam("Operator");
        ((AbstractTeamMixinInterface) team).setEliminated(true);
        team.setColor(Formatting.WHITE);
        team.setPrefix(new LiteralText("[OP] "));
        scoreboard.addPlayerToTeam("senseiwells", team);
        TeamUtils.addNonTeam(team);
        team = scoreboard.addTeam("Spectator");
        ((AbstractTeamMixinInterface) team).setEliminated(true);
        team.setColor(Formatting.DARK_GRAY);
        TeamUtils.addNonTeam(team);
        Team finalTeam = team;
        PlayerUtils.forEveryPlayer(player -> {
            if (player.getScoreboardTeam() == null) {
                scoreboard.addPlayerToTeam(player.getEntityName(), finalTeam);
            }
        });
    }

    public static Path getPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("Teams.json");
    }
}
