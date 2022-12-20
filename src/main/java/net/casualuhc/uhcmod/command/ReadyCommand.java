package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class ReadyCommand {
	private static final SimpleCommandExceptionType NOT_NOW = new SimpleCommandExceptionType(Text.translatable("uhc.lobby.ready.notNow"));
	private static final SimpleCommandExceptionType NO_TEAM = new SimpleCommandExceptionType(Text.translatable("uhc.lobby.ready.noTeam"));
	private static final SimpleCommandExceptionType ALREADY_READY = new SimpleCommandExceptionType(Text.translatable("uhc.lobby.ready.alreadyReady"));

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("ready")
			.then(literal("yes")
				.executes(context -> ready(context, true))
			)
			.then(literal("no")
				.executes(context -> ready(context, false))
			)
			.then(literal("reset").requires(source -> source.hasPermissionLevel(4))
				.executes(context -> {
					context.getSource().getServer().getScoreboard().getTeams().forEach(team -> TeamManager.setReady(team, false));
					return 1;
				})
			)
			.then(literal("awaiting").requires(source -> source.hasPermissionLevel(4))
				.executes(context -> {
					if (!GameManager.isPhase(Phase.READY)) {
						throw NOT_NOW.create();
					}
					ServerCommandSource source = context.getSource();
					source.sendFeedback(Text.literal("The following teams are not ready:"), true);
					for (Team team : TeamManager.getUnreadyTeams()) {
						source.sendFeedback(team.getFormattedName(), true);
					}
					return 1;
				})
			)
		);
	}

	private static int ready(CommandContext<ServerCommandSource> context, boolean isReady) throws CommandSyntaxException {
		if (!GameManager.isPhase(Phase.READY)) {
			throw NOT_NOW.create();
		}
		AbstractTeam team = context.getSource().getPlayerOrThrow().getScoreboardTeam();
		if (TeamManager.shouldIgnoreTeam(team)) {
			throw NO_TEAM.create();
		}
		if (TeamManager.isReady(team)) {
			throw ALREADY_READY.create();
		}
		TeamManager.setReady(team, isReady);
		MutableText readyText = isReady ? Text.translatable("uhc.lobby.ready.isReady", team.getName()) : Text.translatable("uhc.lobby.ready.notReady", team.getName());
		PlayerManager.messageEveryPlayer(readyText.formatted(team.getColor(), Formatting.BOLD));
		TeamManager.checkAllTeamsReady();
		return 1;
	}
}
