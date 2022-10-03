package net.casualuhc.uhcmod.utils;

import com.google.gson.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.data.TeamExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
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
import net.minecraft.world.GameMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TeamUtils {
	private static final Set<AbstractTeam> IGNORED_TEAMS = new HashSet<>();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static boolean shouldIgnoreTeam(AbstractTeam team) {
		return team == null || IGNORED_TEAMS.contains(team);
	}

	public static void unReadyAllTeams() {
		for (Team team : UHCMod.SERVER.getScoreboard().getTeams()) {
			setReady(team, false);
		}
	}

	public static boolean teamHasAlive(AbstractTeam team) {
		MinecraftServer server = UHCMod.SERVER;
		Collection<String> playerNames = team.getPlayerList();
		for (String playerName : playerNames) {
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
			if (player != null && PlayerUtils.isPlayerSurvival(player)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isLastTeam() {
		MinecraftServer server = UHCMod.SERVER;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (PlayerUtils.isPlayerSurvival(playerEntity)) {
				team = team == null ? playerEntity.getScoreboardTeam() : team;
				if (!TeamUtils.shouldIgnoreTeam(team) && team != playerEntity.getScoreboardTeam()) {
					return false;
				}
			}
		}
		return true;
	}

	public static AbstractTeam getLastTeam() {
		MinecraftServer server = UHCMod.SERVER;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (PlayerUtils.isPlayerPlayingInSurvival(playerEntity)) {
				team = playerEntity.getScoreboardTeam();
			}
		}
		return team;
	}

	public static void sendReadyMessage() {
		Text yesMessage = Text.literal("[YES]").formatted(Formatting.BOLD, Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready yes")));
		Text noMessage = Text.literal("[NO]").formatted(Formatting.BOLD, Formatting.RED).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready no")));
		Text readyMessage = Text.literal(
			"""
			%s══════════════════%s
			
			      Is your team ready?
			
			
			""".formatted(ChatColour.GOLD, ChatColour.RESET)
		).append("       ").append(yesMessage).append("        ").append(noMessage).append(
			"""
   
   
			%s══════════════════%s
			""".formatted(ChatColour.GOLD, ChatColour.RESET)
		);
		PlayerUtils.forEveryPlayer(playerEntity -> {
			AbstractTeam team = playerEntity.getScoreboardTeam();
			if (TeamUtils.shouldIgnoreTeam(team)) {
				return;
			}
			playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.MASTER, 1f, 1f);
			playerEntity.sendMessage(readyMessage, false);
		});
	}

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

	public static int forceAddPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		Team team = TeamArgumentType.getTeam(context, "team");
		UHCMod.SERVER.getScoreboard().addPlayerToTeam(player.getEntityName(), team);
		player.sendMessage(Text.literal("You have been added to team ").append(team.getFormattedName()), false);
		context.getSource().sendFeedback(Text.literal("Successfully added to team"), false);
		player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Good Luck!").formatted(Formatting.GOLD, Formatting.BOLD)));
		player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1.0F, 1.0F);
		player.getHungerManager().setSaturationLevel(20F);
		EntityAttributeInstance instance = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (instance != null) {
			instance.removeModifier(PlayerUtils.HEALTH_BOOST);
			instance.addPersistentModifier(new EntityAttributeModifier(PlayerUtils.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
		}
		player.setHealth(player.getMaxHealth());
		PlayerUtils.setPlayerPlaying(player, true);

		for (ServerPlayerEntity playerEntity : UHCMod.SERVER.getPlayerManager().getPlayerList()) {
			if (team.getPlayerList().contains(playerEntity.getEntityName()) && PlayerUtils.isPlayerSurvival(playerEntity) && !player.equals(playerEntity)) {
				player.teleport(
					playerEntity.getWorld(),
					playerEntity.getX(),
					playerEntity.getY(),
					playerEntity.getZ(),
					playerEntity.getYaw(),
					playerEntity.getPitch()
				);
				break;
			}
		}

		player.changeGameMode(GameMode.SURVIVAL);
		return 1;
	}

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
		scoreboard.addPlayerToTeam("senseiwells", operatorTeam);
		operatorTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
		IGNORED_TEAMS.add(operatorTeam);

		Team spectatorTeam = scoreboard.addTeam("Spectator");
		spectatorTeam.setColor(Formatting.DARK_GRAY);
		spectatorTeam.setCollisionRule(AbstractTeam.CollisionRule.NEVER);
		setEliminated(spectatorTeam, true);
		spectatorTeam.setNameTagVisibilityRule(AbstractTeam.VisibilityRule.NEVER);
		IGNORED_TEAMS.add(spectatorTeam);

		PlayerUtils.forEveryPlayer(player -> {
			if (player.getScoreboardTeam() == null) {
				scoreboard.addPlayerToTeam(player.getEntityName(), spectatorTeam);
			}
		});
	}

	public static Path getPath() {
		return FabricLoader.getInstance().getConfigDir().resolve("Teams.json");
	}

	public static boolean isEliminated(AbstractTeam team) {
		return TeamExtension.get(team).isEliminated;
	}

	public static void setEliminated(AbstractTeam team, boolean eliminated) {
		TeamExtension.get(team).isEliminated = eliminated;
	}

	public static boolean isReady(AbstractTeam team) {
		return TeamExtension.get(team).isReady;
	}

	public static void setReady(AbstractTeam team, boolean ready) {
		TeamExtension.get(team).isReady = ready;
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
