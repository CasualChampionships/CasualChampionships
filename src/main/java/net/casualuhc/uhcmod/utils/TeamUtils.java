package net.casualuhc.uhcmod.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TeamUtils {

	private static final Set<AbstractTeam> nonTeams = new HashSet<>();

	public static boolean isNonTeam(AbstractTeam team) {
		return team == null || nonTeams.contains(team);
	}

	public static void addNonTeam(AbstractTeam team) {
		nonTeams.add(team);
	}

	public static void clearNonTeam() {
		nonTeams.clear();
	}

	public static void unReadyAllTeams() {
		for (Team team : UHCMod.UHCServer.getScoreboard().getTeams()) {
			((AbstractTeamMixinInterface) team).setReady(false);
		}
	}

	public static boolean teamHasAlive(AbstractTeam team) {
		MinecraftServer server = UHCMod.UHCServer;
		Collection<String> playerNames = team.getPlayerList();
		for (String playerName : playerNames) {
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(playerName);
			if (player != null && player.interactionManager.getGameMode() == GameMode.SURVIVAL) {
				return true;
			}
		}
		return false;
	}

	public static boolean isLastTeam() {
		MinecraftServer server = UHCMod.UHCServer;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (playerEntity.interactionManager.getGameMode() == GameMode.SURVIVAL) {
				team = team == null ? playerEntity.getScoreboardTeam() : team;
				if (team != playerEntity.getScoreboardTeam()) {
					return false;
				}
			}
		}
		return true;
	}

	public static AbstractTeam getLastTeam() {
		MinecraftServer server = UHCMod.UHCServer;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (playerEntity.interactionManager.getGameMode() == GameMode.SURVIVAL) {
				team = playerEntity.getScoreboardTeam();
			}
		}
		return team;
	}

	public static void sendReadyMessage() {
		Text yesMessage = new LiteralText("[YES]").formatted(Formatting.BOLD, Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready yes")));
		Text noMessage = new LiteralText("[NO]").formatted(Formatting.BOLD, Formatting.RED).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready no")));
		Text readyMessage = new LiteralText(
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
			if (TeamUtils.isNonTeam(team)) {
				return;
			}
			playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.MASTER, 1f, 1f);
			playerEntity.sendMessage(readyMessage, false);
		});
	}

	public static void checkAllTeamsReady() {
		MinecraftServer server = UHCMod.UHCServer;
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
			if (teamHasMember && !((AbstractTeamMixinInterface) team).isReady() && !TeamUtils.isNonTeam(team)) {
				return;
			}
		}
		Phase.START.run();
	}

	public static int forceAddPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		Team team = TeamArgumentType.getTeam(context, "team");
		UHCMod.UHCServer.getScoreboard().addPlayerToTeam(player.getEntityName(), team);
		player.sendMessage(new LiteralText("You have been added to team ").append(team.getFormattedName()), false);
		context.getSource().sendFeedback(new LiteralText("Successfully added to team"), false);
		player.networkHandler.sendPacket(new TitleS2CPacket(new LiteralText("Good Luck!").formatted(Formatting.GOLD, Formatting.BOLD)));
		player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL, SoundCategory.MASTER, 1.0F, 1.0F);
		player.getHungerManager().setSaturationLevel(20F);
		EntityAttributeInstance instance = player.getAttributes().getCustomInstance(EntityAttributes.GENERIC_MAX_HEALTH);
		if (instance != null) {
			instance.removeModifier(GameManager.HEALTH_BOOST);
			instance.addPersistentModifier(new EntityAttributeModifier(GameManager.HEALTH_BOOST, "Health Boost", GameSettings.HEALTH.getValue(), EntityAttributeModifier.Operation.MULTIPLY_BASE));
		}
		player.setHealth(player.getMaxHealth());
		PlayerUtils.setPlayerPlaying(player, true);
		team.getPlayerList().forEach(s ->
			PlayerUtils.forEveryPlayer(playerEntity -> {
				if (playerEntity.getEntityName().equals(s) && playerEntity.interactionManager.getGameMode() == GameMode.SURVIVAL) {
					player.teleport(
						playerEntity.getWorld(),
						playerEntity.getX(),
						playerEntity.getY(),
						playerEntity.getZ(),
						playerEntity.getYaw(),
						playerEntity.getPitch()
					);
					player.changeGameMode(GameMode.SURVIVAL);
				}

			})
		);
		return 1;
	}
}
