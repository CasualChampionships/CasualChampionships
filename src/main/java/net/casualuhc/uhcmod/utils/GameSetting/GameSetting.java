package net.casualuhc.uhcmod.utils.GameSetting;

import java.util.Map;

public class GameSetting<T> {

    private final String name;
    private final String description;
    private final Map<String, T> options;
    private T value;

    protected GameSetting(String name, String description, Map<String, T> options, T defaultValue) {
        this.name = name;
        this.description = description;
        this.options = options;
        this.value = defaultValue;
        GameSettings.gameSettingMap.put(name, this);
    }

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

    public String getDescription() {
        return this.description;
    }

    public static class DoubleGameSetting extends GameSetting<Double> {
        public DoubleGameSetting(String name, String description, Map<String, Double> options, Double defaultValue) {
            super(name, description, options, defaultValue);
        }
    }

    public static class BooleanGameSetting extends GameSetting<Boolean> {
        public BooleanGameSetting(String name, String description, Map<String, Boolean> options, Boolean defaultValue) {
            super(name, description, options, defaultValue);
        }
    }
}
