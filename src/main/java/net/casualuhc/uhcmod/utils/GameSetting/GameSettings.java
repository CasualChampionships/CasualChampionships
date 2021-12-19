package net.casualuhc.uhcmod.utils.GameSetting;

import java.util.HashMap;
import java.util.Map;

public class GameSettings {

    public static final Map<String, GameSetting<?>> gameSettingMap = new HashMap<>();

    public static GameSetting.DoubleGameSetting
        WORLD_BORDER_SPEED = new GameSetting.DoubleGameSetting("border_speed", "This will change the speed of the world border", Map.of("slow", 0.8D, "normal", 1.0D, "fast", 1.2D), 1.0D),
        HEALTH = new GameSetting.DoubleGameSetting("health", "This will change the max amount of health you can have", Map.of("half", -0.5D, "normal", 0.0D, "double", 1.0D, "triple", 2.0D), 0.0D);

    public static GameSetting.BooleanGameSetting
        END_GAME_GLOW = new GameSetting.BooleanGameSetting("end_game_glow", "If enabled this will make all players glow 5 minutes after the world border has reached it's final size", Map.of("on", true, "off", false), false);
}
