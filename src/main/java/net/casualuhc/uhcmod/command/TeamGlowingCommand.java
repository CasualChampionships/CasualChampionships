package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TeamGlowingCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(CommandManager.literal("teamglow").executes(TeamGlowingCommand::run));
	}

	private static int run(CommandContext<ServerCommandSource> context) {
		Entity entity = (context.getSource()).getEntity();
		if (entity instanceof ServerPlayerMixinInterface ei) {
			ei.setGlowingBoolean(!ei.getGlowingBoolean());
			PlayerUtils.forceUpdateGlowing();
		}
		return 1;
	}
}
