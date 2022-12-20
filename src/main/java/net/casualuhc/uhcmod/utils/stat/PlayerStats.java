package net.casualuhc.uhcmod.utils.stat;

import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.minecraft.advancement.Advancement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class PlayerStats {
	private final Map<UHCStat, Double> stats = new HashMap<>();
	private final Set<Advancement> advancements = new HashSet<>();

	public PlayerStats() {
		this.reset();
	}

	public void set(UHCStat stat, double value) {
		this.stats.put(stat, value);
	}

	public void increment(UHCStat stat, double value) {
		this.set(stat, this.get(stat) + value);
	}

	public double get(UHCStat stat) {
		Double value = this.stats.get(stat);
		if (value == null) {
			throw new IllegalStateException("Tried to get non-existent player stat");
		}
		return value;
	}

	public void reset() {
		for (UHCStat stat : UHCStat.values()) {
			this.stats.put(stat, stat.defaultValue);
		}
		this.advancements.clear();
	}

	public void addAdvancement(Advancement advancement) {
		if (UHCAdvancements.isUhcAdvancement(advancement)) {
			this.advancements.add(advancement);
		}
	}

	public Stream<Advancement> getAdvancements() {
		return this.advancements.stream();
	}
}
