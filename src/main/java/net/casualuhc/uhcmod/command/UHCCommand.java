package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class UHCCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("uhc").requires(source -> source.hasPermissionLevel(4) || source.getName().equals("senseiwells"))
			.then(literal("reloadteams")
				.executes(context -> {
					TeamManager.createTeams();
					return 1;
				})
			)
			.then(literal("cancelphase")
				.executes(context -> {
					GameManager.Phase.NONE.run();
					return 1;
				})
			)
			.then(literal("setup")
				.executes(context -> {
					GameManager.Phase.SETUP.run();
					return 1;
				})
			)
			.then(literal("lobby")
				.executes(context -> {
					GameManager.Phase.LOBBY.run();
					return 1;
				})
			)
			.then(literal("start")
				.executes(context -> {
					GameManager.Phase.READY.run();
					return 1;
				})
			)
			.then(literal("forcestart")
				.executes(context -> {
					GameManager.Phase.START.run();
					return 1;
				})
			)
			// This should only be used in case of server restart...
			.then(literal("quietstart")
				.executes(context -> {
					GameManager.currentPhase = GameManager.Phase.ACTIVE;
					return 1;
				})
			)
		);
	}
}
