package net.casualuhc.uhcmod.utils.gamesettings;

import net.casualuhc.uhcmod.UHCMod;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class GameSetting<T> {
	private final ItemStack displayStack;
	private final Map<ItemStack, T> options;
	private final Consumer<GameSetting<T>> callback;
	private T value;

	protected GameSetting(ItemStack name, Map<ItemStack, T> options, T defaultValue, Consumer<GameSetting<T>> callback) {
		this.displayStack = name;
		this.callback = callback;
		this.options = options;
		this.setValueQuietly(defaultValue);

		GameSettings.RULES.put(name, this);
	}

	public String getName() {
		return this.displayStack.getName().getString();
	}

	public ItemStack getDisplay() {
		return this.displayStack;
	}

	public Map<ItemStack, T> getOptions() {
		return this.options;
	}

	public T getValue() {
		return this.value;
	}

	public void setValueFromOption(ItemStack option) {
		T newValue = this.options.get(option);
		if (newValue != null) {
			this.setValue(newValue);
		}
	}

	public void setValue(T value) {
		this.setValueQuietly(value);
		if (this.callback != null) {
			this.callback.accept(this);
		}
	}

	public void setValueQuietly(T value) {
		if (!Objects.equals(this.value, value)) {
			this.value = value;
			this.resetSelected();
			UHCMod.LOGGER.info("Config '%s' has been set to '%s'".formatted(this.getName(), this.value));
		}
	}

	public void resetSelected() {
		this.options.forEach((stack, value) -> {
			if (this.value.equals(value)) {
				NbtCompound nbtCompound = stack.getOrCreateNbt();
				NbtList dummyList = new NbtList();
				dummyList.add(new NbtCompound());
				nbtCompound.put(ItemStack.ENCHANTMENTS_KEY, dummyList);
			} else {
				stack.removeSubNbt(ItemStack.ENCHANTMENTS_KEY);
			}
		});
	}

	public static class DoubleGameSetting extends GameSetting<Double> {
		public DoubleGameSetting(ItemStack name, Map<ItemStack, Double> options, Double defaultValue) {
			this(name, options, defaultValue, null);
		}

		public DoubleGameSetting(ItemStack name, Map<ItemStack, Double> options, Double defaultValue, Consumer<GameSetting<Double>> callback) {
			super(name, options, defaultValue, callback);
		}
	}

	public static class BooleanGameSetting extends GameSetting<Boolean> {
		public BooleanGameSetting(ItemStack name, Map<ItemStack, Boolean> options, Boolean defaultValue) {
			this(name, options, defaultValue, null);
		}

		public BooleanGameSetting(ItemStack name, Map<ItemStack, Boolean> options, Boolean defaultValue, Consumer<GameSetting<Boolean>> callback) {
			super(name, options, defaultValue, callback);
		}
	}

	public static class EnumGameSetting<E extends Enum<?>> extends GameSetting<E> {
		public EnumGameSetting(ItemStack name, Map<ItemStack, E> options, E defaultValue, Consumer<GameSetting<E>> callback) {
			super(name, options, defaultValue, callback);
		}
	}
}
