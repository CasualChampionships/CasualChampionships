package net.casualuhc.uhcmod.utils.GameSetting;

import java.util.HashMap;
import java.util.Map;

public class GameSettings {

    public static final Map<String, GameSetting<?>> gameSettingMap = new HashMap<>();

    public static GameSetting.DoubleGameSetting
        WORLD_BORDER_SPEED = new GameSetting.DoubleGameSetting("border_speed", Map.of("slow", 0.9D, "normal", 1.0D, "fast", 1.1D), 1.0D),
        HEALTH = new GameSetting.DoubleGameSetting("health", Map.of("normal", 0.0D, "double", 1.0D, "triple", 2.0D), 1.0D);

    public static GameSetting.BooleanGameSetting
        END_GAME_GLOW = new GameSetting.BooleanGameSetting("end_game_glow", Map.of("on", true, "off", false), true);
}
