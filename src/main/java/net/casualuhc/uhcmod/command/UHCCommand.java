package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class UHCCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("uhc")
			.then(argument("command", StringArgumentType.word())
				.suggests((context, builder) -> CommandSource.suggestMatching(new String[]{
					"reloadteams",
					"setup",
					"start",
					"lobby"
				}, builder))
				.executes(context -> {
					String command = context.getArgument("command", String.class);
					switch (command) {
						case "reloadteams" -> TeamManager.createTeams();
						case "setup" -> GameManager.Phase.SETUP.run();
						case "lobby" -> GameManager.Phase.LOBBY.run();
						case "start" -> GameManager.Phase.STARTING.run();
						default -> throw new SimpleCommandExceptionType(new LiteralText("Invalid argument!")).create();
					}
					return 0;
				})
			)
		);
	}
}
