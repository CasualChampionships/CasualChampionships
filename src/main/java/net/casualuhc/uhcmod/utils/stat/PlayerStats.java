package net.casualuhc.uhcmod.utils.stat;

import net.casualuhc.arcade.advancements.AdvancementHandler;
import net.minecraft.advancement.Advancement;

import java.util.*;
import java.util.stream.Stream;

public class PlayerStats {
	private final Map<UHCStat, Double> stats = new EnumMap<>(UHCStat.class);
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
		if (AdvancementHandler.isCustom(advancement)) {
			this.advancements.add(advancement);
		}
	}

	public Stream<Advancement> getAdvancements() {
		return this.advancements.stream();
	}
}
