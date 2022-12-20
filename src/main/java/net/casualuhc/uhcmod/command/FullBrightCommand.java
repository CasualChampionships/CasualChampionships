package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.data.PlayerFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;

public class FullBrightCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("fullbright").executes(c -> {
			ServerPlayerEntity player = c.getSource().getPlayerOrThrow();
			PlayerExtension extension = PlayerExtension.get(player);
			extension.toggleFlag(PlayerFlag.FULL_BRIGHT_ENABLED);
			PlayerManager.updateFullBright(player, false);
			return 1;
		}));
	}
}
