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

	public static final String MONGO_URI;
	public static final boolean IS_DEV;

	static {
		GSON = new GsonBuilder().setPrettyPrinting().create();
		CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("UHC Config.json");

		String uri = null;
		boolean dev = true;
		try {
			if (Files.exists(CONFIG_PATH)) {
				String string = Files.readString(CONFIG_PATH);
				JsonObject config = GSON.fromJson(string, JsonObject.class);
				uri = config.get("mongo").getAsString();
				dev = config.get("dev").getAsBoolean();
			}
		} catch (IOException e) {
			UHCMod.LOGGER.error("Failed to read config", e);
		}
		MONGO_URI = uri;
		IS_DEV = dev;
	}

	public static void noop() { }

	public static boolean hasMongo() {
		return MONGO_URI != null;
	}
}
