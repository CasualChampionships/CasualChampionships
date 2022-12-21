package net.casualuhc.uhcmod.utils.stat;

import java.util.Locale;

public enum UHCStat {
	MINESWEEPER_RECORD(UHCStat::minNonNan, Double.NaN, true),
	DAMAGE_DEALT(Double::sum, 0.0, true),
	DAMAGE_TAKEN(Double::sum, 0.0, true),
	KILLS(Double::sum, 0, true),
	DEATHS(Double::sum, 0, true),
	RELOGS(Double::sum, 0, true),
	PLAYS(Double::sum, 0, false);

	public final Merger merger;
	public final double defaultValue;
	public final boolean latest;

	UHCStat(Merger merger, double defaultValue, boolean latest) {
		this.merger = merger;
		this.defaultValue = defaultValue;
		this.latest = latest;
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
