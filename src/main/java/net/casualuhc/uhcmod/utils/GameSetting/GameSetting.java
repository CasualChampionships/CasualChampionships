package net.casualuhc.uhcmod.utils.GameSetting;

import java.util.Map;

public class GameSetting<T> {

    private final String name;
    private final Map<String, T> options;
    private T value;

    protected GameSetting(String name, Map<String, T> options, T defaultValue) {
        this.name = name;
        this.options = options;
        this.value = defaultValue;
        GameSettings.gameSettingMap.put(name, this);
    }

    @SuppressWarnings("unused")
    public String getName() {
        return this.name;
    }

    public Map<String, T> getOptions() {
        return this.options;
    }

    public T getValue() {
        return this.value;
    }

    public void setValueFromOption(String option) {
        T newValue = this.options.get(option);
        if (newValue != null) {
            this.value = newValue;
        }
    }

    public static class DoubleGameSetting extends GameSetting<Double> {
        public DoubleGameSetting(String name, Map<String, Double> options, Double defaultValue) {
            super(name, options, defaultValue);
        }
    }

    public static class BooleanGameSetting extends GameSetting<Boolean> {
        public BooleanGameSetting(String name, Map<String, Boolean> options, Boolean defaultValue) {
            super(name, options, defaultValue);
        }
    }
}
