package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TeamGlowingCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("teamglow").executes(TeamGlowingCommand::run));
	}

	private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity entity = context.getSource().getPlayerOrThrow();
		PlayerExtension extension = PlayerExtension.get(entity);
		extension.shouldGlow = !extension.shouldGlow;
		PlayerManager.forceUpdateGlowing();
		return 1;
	}
}
