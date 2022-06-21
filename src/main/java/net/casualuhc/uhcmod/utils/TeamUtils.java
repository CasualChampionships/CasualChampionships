package net.casualuhc.uhcmod.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.casualuhc.uhcmod.utils.Event.Events;
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
		for (Team team : UHCMod.UHC_SERVER.getScoreboard().getTeams()) {
			((AbstractTeamMixinInterface) team).setReady(false);
		}
	}

	public static boolean teamHasAlive(AbstractTeam team) {
		MinecraftServer server = UHCMod.UHC_SERVER;
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
		MinecraftServer server = UHCMod.UHC_SERVER;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (PlayerUtils.isPlayerSurvival(playerEntity)) {
				team = team == null ? playerEntity.getScoreboardTeam() : team;
				if (!TeamUtils.isNonTeam(team) && team != playerEntity.getScoreboardTeam()) {
					return false;
				}
			}
		}
		return true;
	}

	public static AbstractTeam getLastTeam() {
		MinecraftServer server = UHCMod.UHC_SERVER;
		AbstractTeam team = null;

		for (ServerPlayerEntity playerEntity : server.getPlayerManager().getPlayerList()) {
			if (PlayerUtils.isPlayerSurvival(playerEntity)) {
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
			if (TeamUtils.isNonTeam(team)) {
				return;
			}
			playerEntity.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, SoundCategory.MASTER, 1f, 1f);
			playerEntity.sendMessage(readyMessage, false);
		});
	}

	public static void checkAllTeamsReady() {
		MinecraftServer server = UHCMod.UHC_SERVER;
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
		Events.ON_START.trigger();
	}

	public static int forceAddPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
		Team team = TeamArgumentType.getTeam(context, "team");
		UHCMod.UHC_SERVER.getScoreboard().addPlayerToTeam(player.getEntityName(), team);
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

		for (ServerPlayerEntity playerEntity : UHCMod.UHC_SERVER.getPlayerManager().getPlayerList()) {
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
}
