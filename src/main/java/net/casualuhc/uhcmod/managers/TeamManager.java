package net.casualuhc.uhcmod.managers;

import com.google.gson.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.data.TeamExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TeamManager {
	private static final Set<AbstractTeam> IGNORED_TEAMS = new HashSet<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	/**
	 * Checks whether a team is eliminated from the UHC.
	 *
	 * @param team the team to check.
	 * @return whether the team is eliminated.
	 */
	public static boolean isEliminated(AbstractTeam team) {
		return TeamExtension.get(team).isEliminated;
	}

	/**
	 * Sets a team as eliminated or not.
	 *
	 * @param team the team to eliminate.
	 * @param eliminated whether the team is eliminated.
	 */
	public static void setEliminated(AbstractTeam team, boolean eliminated) {
		TeamExtension.get(team).isEliminated = eliminated;
	}

	/**
	 * Checks whether a team is ready to play UHC.
	 *
	 * @param team the team to check.
	 * @return whether they are ready.
	 */
	public static boolean isReady(AbstractTeam team) {
		return TeamExtension.get(team).isReady;
	}

	/**
	 * Sets a team as ready or not.
	 *
	 * @param team the team to mark as ready or not.
	 * @param ready whether they are ready.
	 */
	public static void setReady(AbstractTeam team, boolean ready) {
		TeamExtension.get(team).isReady = ready;
	}

	/**
	 * Checks whether a team should be ignored (e.i. they aren't playing).
	 *
	 * @param team the team to check.
	 * @return whether the team should be ignored.
	 */
	public static boolean shouldIgnoreTeam(@Nullable AbstractTeam team) {
		return team == null || IGNORED_TEAMS.contains(team);
	}

	/**
	 * Checks whether a team has players that are alive.
	 *
	 * @param team the team to check.
	 * @return whether the team has alive players.
	 */
	public static boolean teamHasAlive(AbstractTeam team) {
		MinecraftServer server = UHCMod.SERVER;
		Collection<String> playerNames = team.getPlayerList();
		for (String playerName : playerNames) {
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
			if (player != null && PlayerManager.isPlayerSurvival(player)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether there is only one team remaining with players alive.
	 *
	 * @return whether there is only one team.
	 */
	public static boolean isLastTeam() {
		MinecraftServer server = UHCMod.SERVER;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (PlayerManager.isPlayerSurvival(playerEntity)) {
				team = team == null ? playerEntity.getScoreboardTeam() : team;
				if (!shouldIgnoreTeam(team) && team != playerEntity.getScoreboardTeam()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Gets any team with players remaining.
	 *
	 * @return a team with remaining players.
	 */
	public static AbstractTeam getAliveTeam() {
		MinecraftServer server = UHCMod.SERVER;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (PlayerManager.isPlayerPlayingInSurvival(playerEntity)) {
				team = playerEntity.getScoreboardTeam();
			}
		}
		return team;
	}

	/**
	 * Checks whether all playing teams are ready.
	 */
	public static void checkAllTeamsReady() {
		MinecraftServer server = UHCMod.SERVER;
		ServerScoreboard scoreboard = server.getScoreboard();
		for (Team team : scoreboard.getTeams()) {
			boolean teamHasMember = false;
			Collection<String> playerNames = team.getPlayerList();
			for (String playerName : playerNames) {
				ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
				if (player != null) {
					teamHasMember = true;
				}
			}
			if (teamHasMember && !isReady(team) && !shouldIgnoreTeam(team)) {
				return;
			}
		}
		EventHandler.onStart();
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
		player.sendMessage(Text.literal("You have been added to team ").append(team.getFormattedName()), false);
		context.getSource().sendFeedback(Text.literal("Successfully added to team"), false);

		PlayerManager.setPlayerForUHC(player);

		for (ServerPlayerEntity playerEntity : UHCMod.SERVER.getPlayerManager().getPlayerList()) {
			if (team.getPlayerList().contains(playerEntity.getEntityName()) && PlayerManager.isPlayerSurvival(playerEntity) && !player.equals(playerEntity)) {
				player.teleport(playerEntity.getWorld(), playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch());
				break;
			}
		}

		return 1;
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
		setEliminated(operatorTeam, true);
		operatorTeam.setColor(Formatting.WHITE);
		operatorTeam.setPrefix(Text.literal("[OP] "));
		for (String operator : Config.OPERATORS) {
			scoreboard.addPlayerToTeam(operator, operatorTeam);
		}
		operatorTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
		IGNORED_TEAMS.add(operatorTeam);

		Team spectatorTeam = scoreboard.addTeam("Spectator");
		spectatorTeam.setColor(Formatting.DARK_GRAY);
		spectatorTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
		setEliminated(spectatorTeam, true);
		spectatorTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
		IGNORED_TEAMS.add(spectatorTeam);

		PlayerManager.forEveryPlayer(player -> {
			if (player.getScoreboardTeam() == null) {
				scoreboard.addPlayerToTeam(player.getEntityName(), spectatorTeam);
			}
		});
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
			public void onReady() {
				unReadyAllTeams();
				sendReadyMessage();
			}
		});
	}

	private static void unReadyAllTeams() {
		for (Team team : UHCMod.SERVER.getScoreboard().getTeams()) {
			setReady(team, false);
		}
	}

	private static void sendReadyMessage() {
		Text yesMessage = Text.literal("[YES]").formatted(Formatting.BOLD, Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready yes")));
		Text noMessage = Text.literal("[NO]").formatted(Formatting.BOLD, Formatting.RED).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready no")));
		Text readyMessage = Text.literal(
			"""
			§6══════════════════§r
			
			      Is your team ready?
			
			
			"""
		).append("       ").append(yesMessage).append("        ").append(noMessage).append(
			"""
   
   
			§6══════════════════§r
			"""
		);
		PlayerManager.forEveryPlayer(playerEntity -> {
			AbstractTeam team = playerEntity.getScoreboardTeam();
			if (shouldIgnoreTeam(team)) {
				return;
			}
			playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.MASTER, 1f, 1f);
			playerEntity.sendMessage(readyMessage, false);
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
			team.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
		}
	}
}
