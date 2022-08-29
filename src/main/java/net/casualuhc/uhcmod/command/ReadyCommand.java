package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class ReadyCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("ready").requires(source -> source.getEntity() != null)
			.then(literal("yes")
				.executes(context -> ready(context, true))
			)
			.then(literal("no")
				.executes(context -> ready(context, false))
			)
			.then(literal("reset").requires(source -> source.hasPermissionLevel(4))
				.executes(context -> {
					context.getSource().getServer().getScoreboard().getTeams().forEach(team -> TeamUtils.setReady(team, false));
					return 1;
				})
			)
		);
	}

	private static int ready(CommandContext<ServerCommandSource> context, boolean isReady) throws CommandSyntaxException {
		if (!GameManager.isPhase(Phase.READY)) {
			throw new SimpleCommandExceptionType(Text.literal("You cannot ready now!")).create();
		}
		AbstractTeam team = context.getSource().getPlayerOrThrow().getScoreboardTeam();
		if (TeamUtils.shouldIgnoreTeam(team)) {
			throw new SimpleCommandExceptionType(Text.literal("You are not on a team!")).create();
		}
		if (TeamUtils.isReady(team)) {
			throw new SimpleCommandExceptionType(Text.literal("You have already readied up!")).create();
		}
		TeamUtils.setReady(team, true);
		String readyText = "%s %s ready!".formatted(team.getName(), isReady ? "is" : "is not");
		PlayerUtils.messageEveryPlayer(Text.literal(readyText).formatted(team.getColor(), Formatting.BOLD));
		TeamUtils.checkAllTeamsReady();
		return 1;
	}
}
