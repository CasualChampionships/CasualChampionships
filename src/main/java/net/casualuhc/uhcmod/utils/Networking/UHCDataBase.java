package net.casualuhc.uhcmod.utils.Networking;

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
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.utils.Config;
import net.casualuhc.uhcmod.utils.PlayerUtils;
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
	public final MongoClient mongoClient;
	private final MongoCollection<Document> playerStats;
	private final MongoCollection<Document> totalPlayerStats;
	private final MongoCollection<Document> teamConfig;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public static final UHCDataBase INSTANCE = new UHCDataBase();

	private UHCDataBase() {
		this.mongoClient = new MongoClient(new MongoClientURI(Config.MONGO_URI));
		MongoDatabase database = this.mongoClient.getDatabase("UHC");
		this.playerStats = database.getCollection(Config.IS_DEV ? "test_player_stats" : "player_stats");
		this.totalPlayerStats = database.getCollection(Config.IS_DEV ? "test_total_player_stats" : "total_player_stats");
		this.teamConfig = database.getCollection("teams");
	}

	public void updateStatDatabase(final String name, final UHCStat stat, final Object newValue) {
		this.executor.execute(() -> {
			Bson filter = eq("name", name);
			MongoCursor<Document> cursor = this.playerStats.find(filter).cursor();
			boolean hasDocument = cursor.hasNext();
			Document document = hasDocument ? cursor.next() : this.createDefaultStat(name);
			document.replace(stat.name, newValue);
			if (hasDocument) {
				this.playerStats.replaceOne(filter, document);
				return;
			}
			this.playerStats.insertOne(document);
		});
	}

	/**
	 * This should only be called when the UHC ends
	 */
	public void updateTotalDataBase() {
		this.executor.execute(() -> {
			for (Document document : this.playerStats.find()) {
				String name = document.getString("name");
				Bson filter = eq("name", name);
				MongoCursor<Document> totalPlayer = this.totalPlayerStats.find(filter).cursor();
				if (!totalPlayer.hasNext()) {
					this.totalPlayerStats.insertOne(document);
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
				this.totalPlayerStats.replaceOne(filter, totalPlayerDocument);
			}
		});
	}

	public void updateStats(ServerPlayerEntity player) {
		if (!PlayerUtils.isPlayerSurvival(player) || !PlayerUtils.isPlayerPlaying(player)) {
			return;
		}
		StatHandler handler = player.getStatHandler();
		String playerName = player.getEntityName();
		for (UHCStat stat : UHCStat.values()) {
			int statValue = stat.applyModifier(handler.getStat(stat.statValue));
			this.updateStatDatabase(playerName, stat, statValue);
		}
	}

	public void downloadTeamFromDataBase() {
		this.executor.execute(() -> {
			try {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				List<JsonObject> jsonObjects = new ArrayList<>();
				for (Document document : this.teamConfig.find()) {
					jsonObjects.add(gson.fromJson(document.toJson(), JsonObject.class));
				}
				File teamJsonFile = TeamManager.getPath().toFile();
				try (FileWriter fileWriter = new FileWriter(teamJsonFile)) {
					fileWriter.write(gson.toJson(jsonObjects));
					fileWriter.flush();
				}
				UHCMod.UHCLogger.info("Successfully downloaded Teams.json");
			}
			catch (MongoException | ClassCastException | IOException ignored) {
				UHCMod.UHCLogger.error("Could not download Teams.json");
			}
		});
	}

	public void incrementWinDataBase(String teamName) {
		this.executor.execute(() -> {
			Bson filter = eq("name", teamName);
			MongoCursor<Document> cursor = this.teamConfig.find(filter).cursor();
			if (!cursor.hasNext()) {
				UHCMod.UHCLogger.error("Winning team cannot be found");
				return;
			}
			Document document = cursor.next();
			int currentWins = document.getInteger("wins");
			document.replace("wins", currentWins + 1);
			this.teamConfig.replaceOne(filter, document);
		});
	}

	public void shutdown() {
		this.executor.execute(this.mongoClient::close);
		this.executor.shutdown();
	}

	private Document createDefaultStat(String name) {
		Document document = new Document("_id", this.playerStats.countDocuments()).append("name", name);
		for (UHCStat stat : UHCStat.values()) {
			document.append(stat.name, 0);
		}
		return document;
	}

	private static final Function<Integer, Integer> DIVIDE_TEN = (integer) -> integer / 10;

	private enum UHCStat {
		DAMAGE_DEALT("damage dealt", Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT), DIVIDE_TEN),
		DAMAGE_TAKEN("damage taken", Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_TAKEN), DIVIDE_TEN),
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
