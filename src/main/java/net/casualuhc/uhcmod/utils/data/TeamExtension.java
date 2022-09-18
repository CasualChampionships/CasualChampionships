package net.casualuhc.uhcmod.utils.data;

import net.minecraft.scoreboard.AbstractTeam;

import java.util.HashMap;
import java.util.Map;

public class TeamExtension {
	private static final Map<AbstractTeam, TeamExtension> TEAMS = new HashMap<>();

	public boolean isReady = false;
	public boolean isEliminated = false;

	private TeamExtension() { }

	public static TeamExtension get(AbstractTeam team) {
		return TEAMS.computeIfAbsent(team, t -> new TeamExtension());
	}
}
