package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.managers.WorldBoarderManager;
import net.casualuhc.uhcmod.utils.GameManagerUtils;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TeamArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class UHCCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("uhc").requires(source -> source.hasPermissionLevel(4) || source.getName().equals("senseiwells"))
			.then(literal("team")
				.then(literal("reload")
					.executes(context -> {
						TeamManager.createTeams();
						return 1;
					})
				)
				.then(literal("forceadd")
					.then(argument("player", EntityArgumentType.entity())
						.then(argument("team", TeamArgumentType.team())
							.executes(TeamUtils::forceAddPlayer)
						)
					)
				)
			)
			.then(literal("phase")
				.then(literal("cancel")
					.executes(context -> {
						Phase.LOBBY.run();
						return 1;
					})
				)
				.then(literal("current")
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayer();
						player.sendMessage(new LiteralText("Current phase is: %d".formatted(GameManager.getCurrentPhase().phaseNumber())), false);
						return 1;
					})
				)
			)
			.then(literal("setup")
				.executes(context -> {
					Phase.SETUP.run();
					return 1;
				})
			)
			.then(literal("lobby")
				.executes(context -> {
					Phase.LOBBY.run();
					return 1;
				})
			)
			.then(literal("vote")
				.executes(context -> {
					Phase.VOTING.run();
					return 1;
				})
			)
			.then(literal("start")
				.executes(context -> {
					Phase.READY.run();
					return 1;
				})
				.then(literal("force")
					.executes(context -> {
						Phase.START.run();
						return 1;
					})
				)
				.then(literal("quiet")
					.executes(context -> {
						GameManager.setCurrentPhase(Phase.ACTIVE);
						WorldBoarderManager.startWorldBorders(Phase.getPhaseThreadGroup(), true);
						GameManagerUtils.setUHCGamerules();
						return 1;
					})
				)
			)
			.then(literal("pvp")
				.then(literal("true").executes(context -> {
					context.getSource().getServer().setPvpEnabled(true);
					return 1;
				}))
				.then(literal("false").executes(context -> {
					context.getSource().getServer().setPvpEnabled(false);
					return 1;
				}))
			)
		);
	}
}