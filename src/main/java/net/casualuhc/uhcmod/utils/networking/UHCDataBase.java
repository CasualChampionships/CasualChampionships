package net.casualuhc.uhcmod.utils.networking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.stat.PlayerStats;
import net.casualuhc.uhcmod.utils.stat.UHCStat;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.minecraft.advancement.AdvancementDisplay;
import org.bson.BasicBSONObject;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
			PLAYER_STATS = database.getCollection(Config.IS_DEV ? "test_player_stats" : "new_player_stats");
			COMBINED_STATS = database.getCollection(Config.IS_DEV ? "test_total_player_stats" : "new_total_player_stats");
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

	/**
	 * This should only be called when the UHC ends
	 */
	public static void updateStats() {
		if (hasMongo()) {
			EXECUTOR.execute(() -> {
				PLAYER_STATS.deleteMany(new BsonDocument());

				PlayerExtension.forEach(extension -> {
					PlayerStats stats = extension.getStats();
					String playerName = extension.getName();
					Document original = new Document("_id", playerName);
					for (UHCStat stat : UHCStat.values()) {
						original.put(stat.id(), stats.get(stat));
					}
					Document latest = new Document(original);

					BasicDBList advancements = stats.getAdvancements().map(a -> {
						BasicBSONObject object = new BasicDBObject();
						object.put("id", a.getId().getPath());
						AdvancementDisplay display = a.getDisplay();
						String itemId = display == null ? "stone" : display.getIcon().getItem().toString();
						object.put("item", itemId);
						return object;
					}).collect(BasicDBList::new, ArrayList::add, ArrayList::addAll);
					latest.put("advancements", advancements);
					PLAYER_STATS.insertOne(latest);

					Bson filter = eq("_id", playerName);
					try (MongoCursor<Document> totalPlayer = COMBINED_STATS.find(filter).cursor()) {
						if (totalPlayer.hasNext()) {
							Document totalPlayerDocument = totalPlayer.next();
							for (UHCStat stat : UHCStat.values()) {
								double value = stat.merger.merge(totalPlayerDocument.getDouble(stat.id()), stats.get(stat));
								totalPlayerDocument.replace(stat.id(), value);
							}
							COMBINED_STATS.replaceOne(filter, totalPlayerDocument);
						} else {
							COMBINED_STATS.insertOne(original);
						}
					}
				});
			});
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

	public static void incrementWinDataBase(final String teamName) {
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

	private static boolean hasMongo() {
		return CLIENT != null;
	}
}
