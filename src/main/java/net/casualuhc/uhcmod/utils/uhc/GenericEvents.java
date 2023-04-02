package net.casualuhc.uhcmod.utils.uhc;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.arcade.broadcaster.Broadcaster;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.*;
import net.casualuhc.arcade.events.server.ServerRegisterCommandEvent;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.command.*;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.UHCManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.stat.UHCStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Items;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class GenericEvents {
	static {
		EventHandler.register(ServerRegisterCommandEvent.class, GenericEvents::listenForCommandRegistration);
		EventHandler.register(PlayerAttackEvent.class, GenericEvents::listenForBoatAttack);
		EventHandler.register(PlayerItemReleaseEvent.class, GenericEvents::listenForBowCooldown);
		EventHandler.register(PlayerAttackEvent.class, GenericEvents::listenForAttackStat);
		EventHandler.register(PlayerDamageEvent.class, GenericEvents::listenForDamageStat);
		EventHandler.register(PlayerVoidDamageEvent.class, GenericEvents::listenForDisableSpectatorVoid);
		EventHandler.register(PlayerChatEvent.class, GenericEvents::listenForTeamMessage);
		EventHandler.register(PlayerItemUseEvent.class, GenericEvents::listenForUseHead);
		EventHandler.register(PlayerItemUseOnEvent.class, GenericEvents::listenForUseHeadOn);
		EventHandler.register(PlayerFallEvent.class, GenericEvents::listenForFallingOutOfLobby);

		Broadcaster.setFormatter(text -> {
			return Text.literal("================\n").formatted(Formatting.GREEN)
				.append(text.copy().formatted(Formatting.GOLD)).append("\n")
				.append(Text.literal("================").formatted(Formatting.GREEN));
		});

		Broadcaster.addMessage(Text.translatable("uhc.broadcast.hostedBy"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.borderWarning"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.spectator"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.noPotions"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.globalChat"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.nighVision"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.teamGlow"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.minesweeper"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.specCommand"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.playerHeads"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.joke"));
		Broadcaster.addMessage(Text.translatable("uhc.broadcast.rules"));
	}

	public static void noop() { }

	private static void listenForCommandRegistration(ServerRegisterCommandEvent event) {
		CommandDispatcher<ServerCommandSource> dispatcher = event.getDispatcher();
		PosCommand.register(dispatcher);
		SpectatorCommand.register(dispatcher);
		CoordsCommand.register(dispatcher);
		UHCCommand.register(dispatcher);
		ReadyCommand.register(dispatcher);
		TeamGlowingCommand.register(dispatcher);
		KitCommand.register(dispatcher);
		FullBrightCommand.register(dispatcher);
		DuelCommand.register(dispatcher);
		MinesweeperCommand.register(dispatcher);
	}

	private static void listenForBoatAttack(PlayerAttackEvent event) {
		if (event.getTarget() instanceof BoatEntity && event.getPlayer().interactionManager.getGameMode() == GameMode.ADVENTURE) {
			event.cancel(0);
		}
	}

	private static void listenForBowCooldown(PlayerItemReleaseEvent event) {
		if (event.getStack().isOf(Items.BOW)) {
			event.getPlayer().getItemCooldownManager().set(Items.BOW, (int) (GameSettings.BOW_COOLDOWN.getValue() * 20));
		}
	}

	private static void listenForAttackStat(PlayerAttackEvent event) {
		if (UHCManager.isGameActive() && event.getTarget() instanceof PlayerEntity) {
			PlayerExtension.get(event.getPlayer()).getStats().increment(UHCStat.DAMAGE_DEALT, event.getDamage());
		}
	}

	private static void listenForDamageStat(PlayerDamageEvent event) {
		if (UHCManager.isGameActive()) {
			PlayerExtension.get(event.getPlayer()).getStats().increment(UHCStat.DAMAGE_TAKEN, event.getAmount());
		}
	}

	private static void listenForDisableSpectatorVoid(PlayerVoidDamageEvent event) {
		if (event.getPlayer().isSpectator()) {
			event.cancel();
		}
	}

	private static void listenForTeamMessage(PlayerChatEvent event) {
		ServerPlayerEntity player = event.getPlayer();
		SignedMessage message = event.getMessage();
		String content = message.getSignedContent();
		if (!PlayerManager.isMessageGlobal(player, content)) {
			Team team = (Team) player.getScoreboardTeam();
			if (team == null) {
				return;
			}
			Text text = team.getFormattedName();
			MessageType.Parameters incoming = MessageType.params(MessageType.TEAM_MSG_COMMAND_INCOMING, player).withTargetName(text);
			MessageType.Parameters outgoing = MessageType.params(MessageType.TEAM_MSG_COMMAND_OUTGOING, player).withTargetName(text);
			PlayerManager.forEveryPlayer(playerEntity -> {
				if (playerEntity == player) {
					playerEntity.sendChatMessage(
						SentMessage.of(message),
						player.shouldFilterText() || playerEntity.shouldFilterText(),
						outgoing
					);
				} else if (team.isEqual(playerEntity.getScoreboardTeam())) {
					playerEntity.sendChatMessage(
						SentMessage.of(message),
						player.shouldFilterText() || playerEntity.shouldFilterText(),
						incoming
					);
				}
			});
			event.cancel();
		}
	}

	private static void listenForUseHead(PlayerItemUseEvent event) {
		if (GameSettings.HEADS_CONSUMABLE.getValue() && event.getStack().isOf(Items.PLAYER_HEAD)) {
			ServerPlayerEntity player = event.getPlayer();
			PlayerManager.giveHeadEffects(player, event.getStack(), event.getHand());
			event.cancel(TypedActionResult.consume(player.getStackInHand(event.getHand())));
		}
	}

	private static void listenForUseHeadOn(PlayerItemUseOnEvent event) {
		if (GameSettings.HEADS_CONSUMABLE.getValue() && event.getStack().isOf(Items.PLAYER_HEAD)) {
			ServerPlayerEntity player = event.getPlayer();
			PlayerManager.giveHeadEffects(player, event.getStack(), event.getContext().getHand());
			event.cancel(ActionResult.CONSUME);
		}
	}

	private static void listenForFallingOutOfLobby(PlayerFallEvent event) {
		if (UHCManager.isPhase(Phase.LOBBY)) {
			ServerPlayerEntity player = event.getPlayer();
			if (player.hasPermissionLevel(2)) {
				return;
			}
			int bottom = UHCMod.SERVER.getOverworld().getTopY() - UHCManager.getLobby().getSize().getY() - 20;
			if (player.getY() < bottom) {
				Vec3d spawn = Config.CURRENT_UHC.getLobbySpawnPos();
				player.teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), player.getYaw(), player.getPitch());
				PlayerManager.grantAdvancement(player, UHCAdvancements.UH_OH);
			}
		}
	}
}
