package net.casualuhc.uhcmod.managers;

import carpet.patches.EntityPlayerMPFake;
import com.google.gson.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.AbstractTeam.CollisionRule;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TeamManager {
	private static final Set<Team> IGNORED_TEAMS = new HashSet<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static CollisionRule collisions = CollisionRule.ALWAYS;

	/**
	 * Checks whether a team should be ignored (e.i. they aren't playing).
	 *
	 * @param team the team to check.
	 * @return whether the team should be ignored.
	 */
	public static boolean shouldIgnoreTeam(@Nullable AbstractTeam team) {
		return team == null || IGNORED_TEAMS.contains((Team) team);
	}

	/**
	 * Forcibly adds a player to a Team (usually mid-game).
	 * Sets the player ready for UHC and teleports them to an existing teammate.
	 *
	 * @param context the command context.
	 * @return the command success.
	 */
	public static int forceAddPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		Team team = TeamArgumentType.getTeam(context, "team");

		UHCMod.SERVER.getScoreboard().addPlayerToTeam(player.getEntityName(), team);
		player.sendMessage(Text.translatable("uhc.game.addedToTeam", team.getFormattedName()), false);
		context.getSource().sendFeedback(Text.literal("Successfully added to team"), false);

		PlayerManager.setPlayerForCTF(player);

		for (ServerPlayerEntity playerEntity : UHCMod.SERVER.getPlayerManager().getPlayerList()) {
			if (team.getPlayerList().contains(playerEntity.getEntityName()) && PlayerManager.isPlayerSurvival(playerEntity) && !player.equals(playerEntity)) {
				player.teleport(playerEntity.getWorld(), playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch());
				break;
			}
		}

		return 1;
	}

	/**
	 * Spawn all fake players.
	 */
	public static void spawnAllPlayers() {
		if (GameManager.isGameActive()) {
			return;
		}
		MinecraftServer server = UHCMod.SERVER;
		net.minecraft.server.PlayerManager playerManager = server.getPlayerManager();
		Vec3d spawnPos = Config.CURRENT_EVENT.getLobbySpawnPos();
		for (Team team : server.getScoreboard().getTeams()) {
			for (String name : team.getPlayerList()) {
				if (playerManager.getPlayer(name) == null) {
					EntityPlayerMPFake.createFake(name, server, spawnPos.x, spawnPos.y, spawnPos.z, 90, 0, World.OVERWORLD, GameMode.ADVENTURE, false);
				}
			}
		}
	}

	/**
	 * Kill all fake players.
	 */
	public static void killAllPlayers() {
		if (!GameManager.isGameActive()) {
			PlayerManager.forEveryPlayer(p -> {
				if (p instanceof EntityPlayerMPFake) {
					p.kill();
				}
			});
		}
	}

	/**
	 * Creates the teams from the Teams json.
	 */
	public static void createTeams() {
		Scoreboard scoreboard = UHCMod.SERVER.getScoreboard();
		for (Team team : scoreboard.getTeams().stream().toList()) {
			scoreboard.removeTeam(team);
		}
		reloadTeams();
		IGNORED_TEAMS.clear();

		Team operatorTeam = scoreboard.addTeam("Operator");
		operatorTeam.setColor(Formatting.WHITE);
		operatorTeam.setPrefix(Text.literal("[OP] "));
		for (String operator : Config.OPERATORS) {
			scoreboard.addPlayerToTeam(operator, operatorTeam);
		}
		operatorTeam.setCollisionRule(CollisionRule.NEVER);
		IGNORED_TEAMS.add(operatorTeam);

		Team spectatorTeam = scoreboard.addTeam("Spectator");
		spectatorTeam.setColor(Formatting.DARK_GRAY);
		spectatorTeam.setCollisionRule(CollisionRule.NEVER);
		IGNORED_TEAMS.add(spectatorTeam);

		PlayerManager.forEveryPlayer(player -> {
			if (player.getScoreboardTeam() == null) {
				scoreboard.addPlayerToTeam(player.getEntityName(), spectatorTeam);
			}
		});
	}

	/**
	 * Sets whether all teams should have collisions enabled or not.
	 *
	 * @param shouldCollide whether players should collide.
	 */
	public static void setCollisions(boolean shouldCollide) {
		collisions = shouldCollide ? CollisionRule.ALWAYS : CollisionRule.NEVER;
		for (Team team : UHCMod.SERVER.getScoreboard().getTeams()) {
			team.setCollisionRule(collisions);
		}
	}

	/**
	 * Gets the path to the Teams json.
	 *
	 * @return the path to the Teams json.
	 */
	public static Path getPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("Teams.json");
	}

	public static void noop() { }

	static {
		EventHandler.register(new UHCEvents() {
			@Override
			public void onSetup() {
				createTeams();
			}

			@Override
			public void onLobby() {
				setCollisions(false);
				for (Team team : IGNORED_TEAMS) {
					team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.ALWAYS);
				}
			}

			@Override
			public void onActive() {
				setCollisions(true);
				for (Team team : IGNORED_TEAMS) {
					team.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
				}
			}
		});
	}

	private static void reloadTeams() {
		Path path = getPath();
		File file = path.toFile();
		if (file.exists()) {
			try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
				JsonArray jsonArray = GSON.fromJson(bufferedReader, JsonArray.class);
				for (JsonElement jsonElement : jsonArray) {
					JsonObject jsonObject = JsonHelper.asObject(jsonElement, "entry");
					createTeamFromJson(jsonObject);
				}
			} catch (JsonParseException | IOException e) {
				UHCMod.LOGGER.error("Could not read teams", e);
			}
		}
	}

	private static void createTeamFromJson(JsonObject json) {
		if (json.has("name") && json.has("colour") && json.has("prefix") && json.has("members")) {
			String teamName = json.get("name").getAsString();
			String colour = json.get("colour").getAsString();
			String prefix = "[%s] ".formatted(json.get("prefix").getAsString());
			JsonArray members = json.get("members").getAsJsonArray();

			Scoreboard scoreboard = UHCMod.SERVER.getScoreboard();
			Team team = scoreboard.getTeam(teamName);
			if (team == null) {
				team = scoreboard.addTeam(teamName);
			}
			team.setPrefix(Text.literal(prefix));
			Formatting formatting = Formatting.byName(colour);
			if (formatting != null) {
				team.setColor(formatting);
			}
			for (JsonElement member : members) {
				scoreboard.addPlayerToTeam(member.getAsString(), team);
			}
			team.setFriendlyFireAllowed(false);
			team.setCollisionRule(collisions);
		}
	}
}
