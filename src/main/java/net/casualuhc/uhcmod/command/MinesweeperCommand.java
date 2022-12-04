package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.utils.screen.MinesweeperScreen;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class MinesweeperCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("minesweeper").executes(c -> {
			c.getSource().getPlayerOrThrow().openHandledScreen(MinesweeperScreen.createScreenFactory());
			return 1;
		}));
	}
}
