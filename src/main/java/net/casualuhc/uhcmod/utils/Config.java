package net.casualuhc.uhcmod.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.casualuhc.uhcmod.UHCMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	private static final Gson GSON;
	private static final Path CONFIG_PATH;

	public static String MONGO_URI;
	public static boolean IS_DEV;

	static {
		GSON = new GsonBuilder().setPrettyPrinting().create();
		CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("UHC Config.json");
	}

	public static void readConfigs() {
		ExceptionUtils.runSafe(() -> {
			if (Files.exists(CONFIG_PATH)) {
				String string = Files.readString(CONFIG_PATH);
				JsonObject config = GSON.fromJson(string, JsonObject.class);
				MONGO_URI = config.get("mongo").getAsString();
				IS_DEV = config.get("dev").getAsBoolean();
				return;
			}
			UHCMod.UHCLogger.error("Failed to read config");
		});
	}

	/*
	 * {
	 *   "mongo": "url",
	 *   "dev": true
	 * }
	 */
}
