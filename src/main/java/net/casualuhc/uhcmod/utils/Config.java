package net.casualuhc.uhcmod.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.casualuhc.uhcmod.UHCMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
	private static final Gson GSON;
	private static final Path CONFIG_PATH;

	public static final BlockPos LOBBY_SPAWN;
	public static final String MONGO_URI;
	public static final boolean IS_DEV;

	static {
		GSON = new GsonBuilder().setPrettyPrinting().create();
		CONFIG_PATH = getConfig("UHC Config.json");

		BlockPos spawn = BlockPos.ORIGIN;
		String uri = null;
		boolean dev = true;
		try {
			if (Files.exists(CONFIG_PATH)) {
				String string = Files.readString(CONFIG_PATH);
				JsonObject config = GSON.fromJson(string, JsonObject.class);
				uri = config.get("mongo").getAsString();
				dev = config.get("dev").getAsBoolean();
				JsonArray array = config.get("lobby_spawn").getAsJsonArray();
				spawn = new BlockPos(
					array.get(0).getAsInt(),
					array.get(1).getAsInt(),
					array.get(2).getAsInt()
				);
			}
		} catch (Exception e) {
			UHCMod.LOGGER.error("Failed to read config", e);
		}
		LOBBY_SPAWN = spawn;
		MONGO_URI = uri;
		IS_DEV = dev;
	}

	public static void noop() { }

	public static boolean hasMongo() {
		return MONGO_URI != null;
	}

	public static Path getConfig(String path) {
		return FabricLoader.getInstance().getConfigDir().resolve(path);
	}
}
