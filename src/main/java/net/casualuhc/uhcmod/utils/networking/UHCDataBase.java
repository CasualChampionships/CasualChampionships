package net.casualuhc.uhcmod.utils.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import static com.mongodb.client.model.Filters.eq;

public class UHCDataBase {
	private static final MongoClient CLIENT;
	private static final MongoCollection<Document> PLAYER_STATS;
	private static final MongoCollection<Document> COMBINED_STATS;
	private static final MongoCollection<Document> TEAM_CONFIG;
	private static final ExecutorService EXECUTOR;

	static {
		if (Config.hasMongo()) {
			CLIENT = new MongoClient(new MongoClientURI(Config.MONGO_URI));
			MongoDatabase database = CLIENT.getDatabase("UHC");
			PLAYER_STATS = database.getCollection(Config.IS_DEV ? "test_player_stats" : "player_stats");
			COMBINED_STATS = database.getCollection(Config.IS_DEV ? "test_total_player_stats" : "total_player_stats");
			TEAM_CONFIG = database.getCollection("teams");
			EXECUTOR = Executors.newSingleThreadExecutor();
		} else {
			CLIENT = null;
			PLAYER_STATS = null;
			COMBINED_STATS = null;
			TEAM_CONFIG = null;
			EXECUTOR = null;
		}
	}

	private UHCDataBase() { }

	public static void updateStatDatabase(final String name, final UHCStat stat, final Object newValue) {
		if (hasMongo()) {
			EXECUTOR.execute(() -> {
				Bson filter = eq("name", name);
				try (MongoCursor<Document> cursor = PLAYER_STATS.find(filter).cursor()) {
					boolean hasDocument = cursor.hasNext();
					Document document = hasDocument ? cursor.next() : createDefaultStat(name);
					document.replace(stat.name, newValue);
					if (hasDocument) {
						PLAYER_STATS.replaceOne(filter, document);
						return;
					}
					PLAYER_STATS.insertOne(document);
				}
			});
		}
	}

	/**
	 * This should only be called when the UHC ends
	 */
	public static void updateTotalDataBase() {
		if (hasMongo()) {
			EXECUTOR.execute(() -> {
				for (Document document : PLAYER_STATS.find()) {
					String name = document.getString("name");
					Bson filter = eq("name", name);
					try (MongoCursor<Document> totalPlayer = COMBINED_STATS.find(filter).cursor()) {
						if (!totalPlayer.hasNext()) {
							COMBINED_STATS.insertOne(document);
							continue;
						}
						Document totalPlayerDocument = totalPlayer.next();
						for (UHCStat stat : UHCStat.values()) {
							int statValue = document.getInteger(stat.name, 0);
							if (statValue <= 0) {
								continue;
							}
							int totalStatValue = totalPlayerDocument.getInteger(stat.name, 0) + statValue;
							totalPlayerDocument.replace(stat.name, totalStatValue);
						}
						COMBINED_STATS.replaceOne(filter, totalPlayerDocument);
					}
				}
			});
		}
	}

	public static void updateStats(ServerPlayerEntity player) {
		if (hasMongo() && PlayerManager.isPlayerPlayingInSurvival(player)) {
			StatHandler handler = player.getStatHandler();
			String playerName = player.getEntityName();
			for (UHCStat stat : UHCStat.values()) {
				int statValue = stat.applyModifier(handler.getStat(stat.statValue));
				updateStatDatabase(playerName, stat, statValue);
			}
		}
	}

	public static void downloadTeamFromDataBase() {
		if (hasMongo()) {
			EXECUTOR.execute(() -> {
				try {
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					List<JsonObject> jsonObjects = new ArrayList<>();
					for (Document document : TEAM_CONFIG.find()) {
						jsonObjects.add(gson.fromJson(document.toJson(), JsonObject.class));
					}
					File teamJsonFile = TeamManager.getPath().toFile();
					try (FileWriter fileWriter = new FileWriter(teamJsonFile)) {
						fileWriter.write(gson.toJson(jsonObjects));
						fileWriter.flush();
					}
					UHCMod.LOGGER.info("Successfully downloaded Teams.json");
				} catch (MongoException | ClassCastException | IOException ignored) {
					UHCMod.LOGGER.error("Could not download Teams.json");
				}
			});
		} else {
			UHCMod.LOGGER.info("There was no mongo to download from");
		}
	}

	public static void incrementWinDataBase(String teamName) {
		if (hasMongo()) {
			EXECUTOR.execute(() -> {
				Bson filter = eq("name", teamName);
				try (MongoCursor<Document> cursor = TEAM_CONFIG.find(filter).cursor()) {
					if (!cursor.hasNext()) {
						UHCMod.LOGGER.error("Winning team cannot be found");
						return;
					}
					Document document = cursor.next();
					int currentWins = document.getInteger("wins");
					document.replace("wins", currentWins + 1);
					TEAM_CONFIG.replaceOne(filter, document);
				}
			});
		}
	}

	public static void shutdown() {
		if (hasMongo()) {
			EXECUTOR.execute(CLIENT::close);
			EXECUTOR.shutdown();
		}
	}

	private static Document createDefaultStat(String name) {
		Document document = new Document("_id", PLAYER_STATS.countDocuments()).append("name", name);
		for (UHCStat stat : UHCStat.values()) {
			document.append(stat.name, 0);
		}
		return document;
	}

	private static boolean hasMongo() {
		return CLIENT != null;
	}

	private enum UHCStat {
		DAMAGE_DEALT("damage dealt", Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT), i -> i / 10),
		DAMAGE_TAKEN("damage taken", Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_TAKEN), i -> i / 10),
		KILLS("kills", Stats.KILLED.getOrCreateStat(EntityType.PLAYER), null),
		DEATHS("deaths", Stats.CUSTOM.getOrCreateStat(Stats.DEATHS), null),
		;

		private final String name;
		private final Stat<?> statValue;
		private final Function<Integer, Integer> statModifier;

		UHCStat(String name, Stat<?> statId, Function<Integer, Integer> statModifier) {
			this.name = name;
			this.statValue = statId;
			this.statModifier = statModifier;
		}

		public int applyModifier(int start) {
			return this.statModifier == null ? start : this.statModifier.apply(start);
		}
	}
}
