package net.casualuhc.uhcmod.utils.uhc;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import net.casualuhc.uhcmod.UHCMod;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
	private static final Gson GSON;
	private static final Path CONFIG_PATH;

	public static final List<String> OPERATORS;
	public static final Event CURRENT_EVENT;
	public static final String MONGO_URI;
	public static final boolean IS_DEV;

	static {
		GSON = new GsonBuilder().setPrettyPrinting().create();
		CONFIG_PATH = getConfig("UHC Config.json");

		List<String> operators = new ArrayList<>();
		Event event = Event.DEFAULT;
		String uri = null;
		boolean dev = true;
		try {
			if (Files.exists(CONFIG_PATH)) {
				String string = Files.readString(CONFIG_PATH);
				JsonObject config = GSON.fromJson(string, JsonObject.class);
				JsonElement mongo = config.get("mongo");
				if (mongo != null) {
					uri = mongo.getAsString();
				}
				dev = config.get("dev").getAsBoolean();
				config.get("operators").getAsJsonArray().forEach(e -> operators.add(e.getAsString()));
				JsonElement element = config.get("event");
				if (element != null && !element.getAsString().equals("default")) {
					event = UHCMod.getEvent(element.getAsString());
					if (event == null) {
						UHCMod.LOGGER.error("Invalid event name: {}", element.getAsString());
						event = Event.DEFAULT;
					}
				}
			}
		} catch (Exception e) {
			UHCMod.LOGGER.error("Failed to read config", e);
		}
		OPERATORS = ImmutableList.copyOf(operators);
		CURRENT_EVENT = event;
		MONGO_URI = uri;
		IS_DEV = dev;
	}

	/**
	 * Whether the UHC has a mongo database.
	 *
	 * @return whether mongo exists.
	 */
	public static boolean hasMongo() {
		return MONGO_URI != null;
	}

	/**
	 * Gets a file from the config folder.
	 *
	 * @param path the subpath.
	 * @return the Path.
	 */
	public static Path getConfig(String path) {
		return FabricLoader.getInstance().getConfigDir().resolve(path);
	}

	public static void noop() { }
}
