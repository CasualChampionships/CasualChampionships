package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.literal;

public class DuelCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("duel").executes(c -> {
			// TODO: Implement duels
			ServerPlayerEntity player = c.getSource().getPlayerOrThrow();
			PlayerUtils.grantAdvancement(player, UHCAdvancements.NOT_NOW);
			return 1;
		}));
	}
}
