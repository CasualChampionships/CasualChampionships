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
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class UHCDataBase {
	public final MongoClient mongoClient;
	private final MongoCollection<Document> playerStats;
	private final MongoCollection<Document> teamConfig;
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	public static final UHCDataBase INSTANCE = new UHCDataBase();

	private UHCDataBase() {
		this.mongoClient = new MongoClient(new MongoClientURI("mongodb+srv://admin:Q9RtqgHBcxXW6h2@cluster0.w6iug.mongodb.net/UHC?retryWrites=true&w=majority&ssl=true&ssl_cert_reqs=CERT_NONE"));
		MongoDatabase database = this.mongoClient.getDatabase("UHC");
		this.playerStats = database.getCollection("player_stats");
		this.teamConfig = database.getCollection("teams");
		this.startUpdatingStats();
	}

	public void updateStatDatabase(final String name, final Stat stat, final Object newValue) {
		this.executor.execute(() -> {
			Bson filter = eq("name", name);
			MongoCursor<Document> cursor = this.playerStats.find(filter).cursor();
			boolean hasDocument = cursor.hasNext();
			Document document = hasDocument ? cursor.next() : this.createDefaultStat(name);
			document.replace(stat.name, newValue);
			if (hasDocument) {
				this.playerStats.replaceOne(filter, document);
			}
			else {
				this.playerStats.insertOne(document);
			}
		});
	}

	public void incrementStatDatabase(final String name, final Stat stat, final int incrementAmount) {
		this.executor.execute(() -> {
			Bson filter = eq("name", name);
			MongoCursor<Document> cursor = this.playerStats.find(filter).cursor();
			boolean hasDocument = cursor.hasNext();
			Document document = hasDocument ? cursor.next() : this.createDefaultStat(name);
			int currentStat = document.get(stat.name, Integer.class);
			document.replace(stat.name, currentStat + incrementAmount);
			if (hasDocument) {
				this.playerStats.replaceOne(filter, document);
			}
			else {
				this.playerStats.insertOne(document);
			}
		});
	}

	private Document createDefaultStat(String name) {
		Document document = new Document("_id", this.playerStats.countDocuments()).append("name", name);
		for (Stat stat : Stat.values()) {
			document.append(stat.name, 0);
		}
		return document;
	}

	public void initialiseStats(ServerPlayerEntity player) {
		StatHandler handler = player.getStatHandler();
		List<Stat> stats = Arrays.stream(Stat.values()).filter(stat -> {
			if (stat.statId == null) {
				return false;
			}
			int statValue = handler.getStat(Stats.CUSTOM.getOrCreateStat(stat.statId));
			return statValue == 0;
		}).collect(Collectors.toList());
		if (!stats.isEmpty()) {
			this.syncInGameStat(player, stats);
		}
	}

	private void syncInGameStat(final ServerPlayerEntity player, final List<Stat> stats) {
		this.executor.execute(() -> {
			String name = player.getEntityName();
			Bson filter = eq("name", name);
			MongoCursor<Document> cursor = this.playerStats.find(filter).cursor();
			boolean hasDocument = cursor.hasNext();
			Document document = hasDocument ? cursor.next() : this.createDefaultStat(name);
			if (!hasDocument) {
				this.playerStats.insertOne(document);
				return;
			}
			for (Stat stat : stats) {
				if (stat.statId == null) {
					continue;
				}
				int databaseStat = document.get(stat.name, Integer.class) * 10;
				UHCMod.UHCServer.execute(() -> player.getStatHandler().setStat(player, Stats.CUSTOM.getOrCreateStat(stat.statId), databaseStat));
			}
		});
	}

	public void updateStats(ServerPlayerEntity player) {
		StatHandler handler = player.getStatHandler();
		String playerName = player.getEntityName();
		int damageTaken = handler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_TAKEN)) / 10;
		this.updateStatDatabase(playerName, Stat.DAMAGE_TAKEN, damageTaken);

		int damageGiven = handler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT)) / 10;
		this.updateStatDatabase(playerName, Stat.DAMAGE_GIVEN, damageGiven);
	}

	private void startUpdatingStats() {
		Thread thread = new Thread(() -> {
			boolean running = true;
			while (running) {
				PlayerUtils.forEveryPlayer(player -> {
					if (player.interactionManager.getGameMode() == GameMode.SURVIVAL && PlayerUtils.isPlayerPlaying(player)) {
						this.updateStats(player);
					}
				});
				if (GameManager.isPhase(Phase.END)) {
					return;
				}
				try {
					Thread.sleep(600000);
				}
				catch (InterruptedException ignored) {
					running = false;
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public void downloadTeamFromDataBase() {
		this.executor.execute(() -> {
			try {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				List<JsonObject> jsonObjects = new ArrayList<>();
				for (Document doc : this.teamConfig.find()) {
					jsonObjects.add(gson.fromJson(doc.toJson(), JsonObject.class));
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
			int currentWins = document.get("wins", Integer.class);
			document.replace("wins", currentWins + 1);
			this.teamConfig.replaceOne(filter, document);
		});
	}

	public void shutdown() {
		this.executor.execute(this.mongoClient::close);
		this.executor.shutdown();
	}

	public enum Stat {
		DAMAGE_GIVEN("damage dealt", Stats.DAMAGE_DEALT),
		DAMAGE_TAKEN("damage taken", Stats.DAMAGE_TAKEN),
		DEATHS("deaths"),
		KILLS("kills");

		public final String name;
		public final Identifier statId;

		Stat(String name, Identifier statId) {
			this.name = name;
			this.statId = statId;
		}

		Stat(String name) {
			this(name, null);
		}
	}
}
