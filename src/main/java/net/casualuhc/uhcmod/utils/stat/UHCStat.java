package net.casualuhc.uhcmod.utils.stat;

import java.util.Locale;

public enum UHCStat {
	MINESWEEPER_RECORD(UHCStat::minNonNan, Double.NaN),
	DAMAGE_DEALT(Double::sum, 0.0),
	DAMAGE_TAKEN(Double::sum, 0.0),
	KILLS(Double::sum, 0),
	DEATHS(Double::sum, 0),
	RELOGS(Double::sum, 0),
	PLAYS(Double::sum, 0);

	public final Merger merger;
	public final double defaultValue;

	UHCStat(Merger merger, double defaultValue) {
		this.merger = merger;
		this.defaultValue = defaultValue;
	}

	public String id() {
		return this.name().toLowerCase(Locale.ROOT);
	}

	private static double minNonNan(double oldValue, double newValue) {
		return Double.isNaN(oldValue) ? newValue : Math.min(oldValue, newValue);
	}

	@FunctionalInterface
	public interface Merger {
		double merge(double oldValue, double newValue);
	}
}
