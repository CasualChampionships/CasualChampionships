package net.casualuhc.uhcmod.uhcs;

import net.casualuhc.arcade.advancements.AdvancementHandler;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.PlayerBlockInteractionEvent;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.UHCManager;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.casualuhc.uhcmod.utils.uhc.UHC;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EasterUHC implements UHC {
	private static final Advancement EASTER_EGG_HUNTER;

	static {
	 	EASTER_EGG_HUNTER = Advancement.Builder.create().parent(UHCAdvancements.OFFICIALLY_BORED).display(
			Items.EGG,
			Text.translatable("uhc.advancements.easterEggHunter"),
			Text.translatable("uhc.advancements.easterEggHunter.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/easter_egg_hunter"));
	}

	/**
	 * Returns the message displayed on the lobby boss bar.
	 *
	 * @return the message to display.
	 */
	@Override
	public MutableText getBossBarMessage() {
		return UHC.super.getBossBarMessage();
	}

	/**
	 * Returns the colour the lobby boss bar should be.
	 *
	 * @return the boss bar colour.
	 */
	@Override
	public BossBar.Color getBossBarColour() {
		return BossBar.Color.PINK;
	}

	/**
	 * Returns the resource pack properties
	 *
	 * @return the pack properties.
	 */
	@Nullable
	@Override
	public MinecraftServer.ServerResourcePackProperties getResourcePack() {
		return new MinecraftServer.ServerResourcePackProperties(
			"https://download.mc-packs.net/pack/745b75724d8869708d450e7790fb1ed4d5a5bcec.zip",
			"745b75724d8869708d450e7790fb1ed4d5a5bcec",
			true, Text.literal("Provides necessary features for UHC")
		);
	}

	/**
	 * Gets the texture for the Golden Head item.
	 *
	 * @return the texture for the Golden Head item.
	 */
	@Override
	public String getGoldenHeadTexture() {
		return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY3OWVmYzU5ZTIyZmNmMzRmNzQ0OGJmN2FiNjY2NGY3OTljM2RmZjY1NmNmNDgzMDk4YmUzNmM5YWUxIn19fQ==";
	}

	/**
	 * Returns the name of the lobby.
	 *
	 * @return the name of the lobby.
	 */
	@Override
	public String getLobbyName() {
		return "easter_lobby";
	}

	/**
	 * Returns the spawning position for the lobby.
	 */
	@Override
	public Vec3d getLobbySpawnPos() {
		return new Vec3d(0.5, 264, 0.5);
	}

	/**
	 * Invoked after all mangers have been initialised.
	 */
	@Override
	public void load() {
		AdvancementHandler.register(EASTER_EGG_HUNTER);

		Map<UUID, Set<BlockPos>> eggs = new HashMap<>();
		EventHandler.register(PlayerBlockInteractionEvent.class, event -> {
			if (!UHCManager.isPhase(Phase.LOBBY)) {
				return;
			}
			BlockPos pos = event.getResult().getBlockPos();
			BlockState state = event.getLevel().getBlockState(pos);
			if (!state.isOf(Blocks.PLAYER_HEAD)) {
				return;
			}

			ServerPlayerEntity player = event.getPlayer();
			Set<BlockPos> gotten = eggs.computeIfAbsent(player.getUuid(), id -> new HashSet<>());
			if (gotten.add(pos)) {
				player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), SoundCategory.MASTER, 1.0F, 1.0F);
				player.sendMessage(Text.translatable("uhc.advancements.easterEggHunter.found", gotten.size(), 10).formatted(Formatting.DARK_AQUA));
				if (gotten.size() >= 10) {
					PlayerManager.grantAdvancement(event.getPlayer(), EASTER_EGG_HUNTER);
				}
			}
			player.swingHand(player.preferredHand, true);
			event.cancel(ActionResult.SUCCESS);
		});
	}
}
