package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
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

	public static void sendReadyMessage() {
		Text yesMessage = new LiteralText("YES").formatted(Formatting.BOLD, Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready yes")));
		Text noMessage = new LiteralText("NO").formatted(Formatting.BOLD, Formatting.RED).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ready no")));
		Text readyMessage = new LiteralText(
				"""
				%s========================================= %s
				
				Is your team ready?
				
				
				""".formatted(ChatColour.GOLD, ChatColour.RESET)
		).append(yesMessage).append("           ").append(noMessage).append(
				"""
				
				
				%s========================================= %s
				""".formatted(ChatColour.GOLD, ChatColour.RESET)
		);
		PlayerUtils.forEveryPlayer(playerEntity -> {
			AbstractTeam team = playerEntity.getScoreboardTeam();
			if (TeamUtils.isNonTeam(team)) {
				return;
			}
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
			if (teamHasMember && !((AbstractTeamMixinInterface) team).isReady() && !isNonTeam(team)) {
				return;
			}
		}
		GameManager.Phase.START.run();
	}
}
