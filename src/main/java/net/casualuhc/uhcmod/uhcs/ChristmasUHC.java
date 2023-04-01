package net.casualuhc.uhcmod.uhcs;

import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.PlayerPackLoadEvent;
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit;
import net.casualuhc.arcade.scheduler.Scheduler;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.events.uhc.UHCLobbyEvent;
import net.casualuhc.uhcmod.events.uhc.UHCStartEvent;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.UHCManager;
import net.casualuhc.uhcmod.utils.uhc.ItemUtils;
import net.casualuhc.uhcmod.utils.uhc.UHC;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer.ServerResourcePackProperties;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChristmasUHC implements UHC {
	private static final Vec3d LOBBY_SPAWN = new Vec3d(2, 272, 9);

	private static final ServerResourcePackProperties PROPERTIES = new ServerResourcePackProperties(
		"https://download.mc-packs.net/pack/19be7fedc4f16f68e406b6c76dd6c9cb652e168c.zip",
		"19be7fedc4f16f68e406b6c76dd6c9cb652e168c",
		false, Text.literal("This texture pack provides some festive features!")
	);

	@Override
	public MutableText getBossBarMessage() {
		return Text.translatable("uhc.lobby.welcome").formatted(Formatting.RED, Formatting.BOLD)
			.append(" ")
			.append(Text.literal("Casual").formatted(Formatting.WHITE, Formatting.BOLD))
			.append(" ")
			.append(Text.literal("UHC").formatted(Formatting.RED, Formatting.BOLD));
	}

	@Override
	public BossBar.Color getBossBarColour() {
		return BossBar.Color.WHITE;
	}

	@Nullable
	@Override
	public ServerResourcePackProperties getResourcePack() {
		return PROPERTIES;
	}

	@Override
	public String getGoldenHeadTexture() {
		return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNhYWQ4NmM3MDhlYjI3NzczYTY0ZjkzNDc5ZTM5ZjA0NDJhNWNlMDg2YjYzMjk2YzdiN2QxY2Y1MTE2MDk1NiJ9fX0=";
	}

	@Override
	public String getLobbyName() {
		return "christmas_lobby";
	}

	@Override
	public Vec3d getLobbySpawnPos() {
		return LOBBY_SPAWN;
	}

	@Override
	public void load() {
		EventHandler.register(PlayerPackLoadEvent.class, event -> {
			if (!UHCManager.isGameActive()) {
				Scheduler.schedule(3, MinecraftTimeUnit.Seconds, () -> PlayerManager.playLobbyMusic(event.getPlayer(), 250 * 20));

				PlayerInventory inventory = event.getPlayer().getInventory();
				if (inventory.isEmpty()) {
					inventory.armor.set(3, ItemUtils.translatableNamed(Items.CARVED_PUMPKIN, "uhc.christmas.santaHat"));
				}
			}
		});
		EventHandler.register(UHCLobbyEvent.class, event -> {
			makeLobbySnowy();
			UHCMod.SERVER.getOverworld().setWeather(0, 6000, true, false);
		});
		EventHandler.register(UHCStartEvent.class, event -> {
			UHCMod.SERVER.getOverworld().setWeather(24000, 0, false, false);
		});
	}

	private static void makeLobbySnowy() {
		Vec3i dimensions = UHCManager.getLobby().getSize();
		int topY = UHCMod.SERVER.getOverworld().getTopY();
		int minX = -dimensions.getX() / 2;
		int minY = UHCMod.SERVER.getOverworld().getTopY() - dimensions.getY() - 10;
		int minZ = -dimensions.getZ() / 2;

		BlockBox blockBox = BlockBox.create(new Vec3i(minX, minY, minZ), new Vec3i(-minX, topY, -minZ));

		ServerWorld serverWorld = UHCMod.SERVER.getOverworld();
		List<Chunk> list = new ArrayList<>();

		for (int j = ChunkSectionPos.getSectionCoord(blockBox.getMinZ()); j <= ChunkSectionPos.getSectionCoord(blockBox.getMaxZ()); ++j) {
			for (int k = ChunkSectionPos.getSectionCoord(blockBox.getMinX()); k <= ChunkSectionPos.getSectionCoord(blockBox.getMaxX()); ++k) {
				Chunk chunk = serverWorld.getChunk(k, j, ChunkStatus.FULL, true);
				list.add(chunk);
			}
		}

		Registry<Biome> biomeRegistry = UHCMod.SERVER.getRegistryManager().get(RegistryKeys.BIOME);
		Biome biome = biomeRegistry.get(BiomeKeys.SNOWY_TAIGA);
		RegistryEntry<Biome> biomeEntry = biomeRegistry.getEntry(biome);

		for (Chunk chunk : list) {
			chunk.populateBiomes(
				(x, y, z, noise) -> {
					int i = BiomeCoords.toBlock(x);
					int j = BiomeCoords.toBlock(y);
					int k = BiomeCoords.toBlock(z);
					if (blockBox.contains(i, j, k)) {
						return biomeEntry;
					}
					return chunk.getBiomeForNoiseGen(x, y, z);
				},
				serverWorld.getChunkManager().getNoiseConfig().getMultiNoiseSampler()
			);
			chunk.setNeedsSaving(true);
		}
		serverWorld.getChunkManager().threadedAnvilChunkStorage.sendChunkBiomePackets(list);
	}
}
