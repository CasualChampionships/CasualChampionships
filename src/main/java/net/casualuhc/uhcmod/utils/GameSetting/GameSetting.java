package net.casualuhc.uhcmod.utils.GameSetting;

import net.casualuhc.uhcmod.UHCMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameSetting<T> {
	private final NamedItemStack namedItemStack;
	private final Map<NamedItemStack, T> options;
	private final Consumer<GameSetting<T>> callback;
	private T value;

	protected GameSetting(NamedItemStack name, Map<NamedItemStack, T> options, T defaultValue, Consumer<GameSetting<T>> callback) {
		this.namedItemStack = name;
		this.options = options;
		this.callback = callback;
		this.setValueQuietly(defaultValue);
		GameSettings.gameSettingMap.put(name.name, this);
	}

	public String getName() {
		return this.namedItemStack.name;
	}

	public ItemStack getStack() {
		return this.namedItemStack.stack;
	}

	public Map<NamedItemStack, T> getOptions() {
		return this.options;
	}

	public T getValue() {
		return this.value;
	}

	public void setValueFromOption(String option) {
		T newValue = this.options.get(new NamedItemStack(option, Items.AIR));
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
			UHCMod.UHCLogger.info("Config '%s' has been set to '%s'".formatted(this.getName(), this.value));
		}
	}

	public void resetSelected() {
		boolean hasSelection = false;
		for (Map.Entry<NamedItemStack, T> entry : this.options.entrySet()) {
			if (!hasSelection && this.value.equals(entry.getValue())) {
				NbtCompound nbtCompound = entry.getKey().stack.getOrCreateNbt();
				NbtList dummyList = new NbtList();
				dummyList.add(new NbtCompound());
				nbtCompound.put(ItemStack.ENCHANTMENTS_KEY, dummyList);
				continue;
			}
			entry.getKey().stack.removeSubNbt(ItemStack.ENCHANTMENTS_KEY);
		}
	}

	public static class DoubleGameSetting extends GameSetting<Double> {
		public DoubleGameSetting(NamedItemStack name, Map<NamedItemStack, Double> options, Double defaultValue) {
			this(name, options, defaultValue, null);
		}

		public DoubleGameSetting(NamedItemStack name, Map<NamedItemStack, Double> options, Double defaultValue, Consumer<GameSetting<Double>> callback) {
			super(name, options, defaultValue, callback);
		}
	}

	public static class BooleanGameSetting extends GameSetting<Boolean> {
		public BooleanGameSetting(NamedItemStack name, Map<NamedItemStack, Boolean> options, Boolean defaultValue) {
			this(name, options, defaultValue, null);
		}

		public BooleanGameSetting(NamedItemStack name, Map<NamedItemStack, Boolean> options, Boolean defaultValue, Consumer<GameSetting<Boolean>> callback) {
			super(name, options, defaultValue, callback);
		}
	}

	public static class EnumGameSetting<E extends Enum<?>> extends GameSetting<E> {
		public EnumGameSetting(NamedItemStack name, Map<NamedItemStack, E> options, E defaultValue, Consumer<GameSetting<E>> callback) {
			super(name, options, defaultValue, callback);
		}
	}

	public static class NamedItemStack {
		static Function<String, Text> defaultFormatter = string -> {
			StringBuilder builder = new StringBuilder();

			String[] strings = string.split("_");
			for (String sub : strings) {
				builder.append(sub.substring(0, 1).toUpperCase()).append(sub.substring(1)).append(" ");
			}

			return new LiteralText(builder.toString().trim()).formatted(Formatting.BOLD, Formatting.GOLD);
		};

		public final String name;
		public final ItemStack stack;

		private NamedItemStack(String name, Item item) {
			this(name, item, defaultFormatter);
		}

		private NamedItemStack(String name, Item item, Function<String, Text> nameFunction) {
			this.name = name;
			this.stack = item.getDefaultStack();

			this.stack.setCustomName(nameFunction.apply(name));
		}

		public static NamedItemStack of(String name, Item item) {
			return new NamedItemStack(name, item);
		}

		public static NamedItemStack of(String name, Item item, Function<String, Text> function) {
			return new NamedItemStack(name, item, function);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof NamedItemStack namedStack) {
				return this.name.equals(namedStack.name);
			}
			if (obj instanceof String) {
				return this.name.equals(obj);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}
	}
}
