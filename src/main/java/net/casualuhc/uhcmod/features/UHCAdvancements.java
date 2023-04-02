package net.casualuhc.uhcmod.features;

import net.casualuhc.arcade.advancements.AdvancementHandler;
import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.player.*;
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit;
import net.casualuhc.arcade.scheduler.Scheduler;
import net.casualuhc.uhcmod.events.uhc.UHCEndEvent;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.UHCManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.screen.MinesweeperScreen;
import net.casualuhc.uhcmod.utils.stat.PlayerStats;
import net.casualuhc.uhcmod.utils.stat.UHCStat;
import net.casualuhc.uhcmod.utils.uhc.OneTimeAchievement;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class that hard codes all the advancements for UHC.
 * <p>
 * Because I don't datapack :).
 */
public class UHCAdvancements {
	public static final Advancement ROOT;
	public static final Advancement FIRST_BLOOD;
	public static final Advancement EARLY_EXIT;
	public static final Advancement MOSTLY_HARMLESS;
	public static final Advancement HEAVY_HITTER;
	public static final Advancement WINNER;
	public static final Advancement COMBAT_LOGGER;
	public static final Advancement ON_THE_EDGE;
	public static final Advancement THATS_NOT_DUSTLESS;
	public static final Advancement PARKOUR_MASTER;
	public static final Advancement WORLD_RECORD_PACE;
	public static final Advancement THATS_EMBARRASSING;
	public static final Advancement BUSTED;
	public static final Advancement DEMOLITION_EXPERT;
	public static final Advancement OK_WE_BELIEVE_YOU_NOW;
	public static final Advancement FALLING_BLOCK;
	public static final Advancement DREAM_LUCK;
	public static final Advancement BROKEN_ANKLES;
	public static final Advancement SKILL_ISSUE;
	public static final Advancement SOLOIST;
	public static final Advancement LDAP;
	public static final Advancement NOT_NOW;
	public static final Advancement OFFICIALLY_BORED;
	public static final Advancement FIND_THE_BUTTON;
	public static final Advancement DISTRACTED;
	public static final Advancement UH_OH;
	public static final Advancement BASICALLY;

	static {
		ROOT = Advancement.Builder.create().display(
			Items.GOLDEN_APPLE,
			Text.translatable("uhc.advancements.root"),
			Text.translatable("uhc.advancements.root.desc"),
			new Identifier("textures/gui/advancements/backgrounds/adventure.png"),
			AdvancementFrame.TASK,
			false, false, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/root"));

		FIRST_BLOOD = Advancement.Builder.create().parent(ROOT).display(
			Items.IRON_SWORD,
			Text.translatable("uhc.advancements.firstBlood"),
			Text.translatable("uhc.advancements.firstBlood.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/first_blood"));

		EARLY_EXIT = Advancement.Builder.create().parent(ROOT).display(
			Items.POISONOUS_POTATO,
			Text.translatable("uhc.advancements.earlyExit"),
			Text.translatable("uhc.advancements.earlyExit.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/early_exit"));

		MOSTLY_HARMLESS = Advancement.Builder.create().parent(EARLY_EXIT).display(
			Items.FEATHER,
			Text.translatable("uhc.advancements.mostlyHarmless"),
			Text.translatable("uhc.advancements.mostlyHarmless.desc"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/mostly_harmless"));

		HEAVY_HITTER = Advancement.Builder.create().parent(FIRST_BLOOD).display(
			Items.ANVIL,
			Text.translatable("uhc.advancements.heavyHitter"),
			Text.translatable("uhc.advancements.heavyHitter.desc"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/heavy_hitter"));

		WINNER = Advancement.Builder.create().parent(FIRST_BLOOD).display(
			Items.TOTEM_OF_UNDYING,
			Text.translatable("uhc.advancements.winner"),
			Text.translatable("uhc.advancements.winner.desc"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/winner"));

		COMBAT_LOGGER = Advancement.Builder.create().parent(ROOT).display(
			Items.WOODEN_SWORD,
			Text.translatable("uhc.advancements.combatLogger"),
			Text.translatable("uhc.advancements.combatLogger.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/combat_logger"));

		THATS_NOT_DUSTLESS = Advancement.Builder.create().parent(ROOT).display(
			Items.REDSTONE,
			Text.translatable("uhc.advancements.notDustless"),
			Text.translatable("uhc.advancements.notDustless.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/thats_not_dustless"));

		PARKOUR_MASTER = Advancement.Builder.create().parent(ROOT).display(
			Items.NETHERITE_BOOTS,
			Text.translatable("uhc.advancements.parkourMaster"),
			Text.translatable("uhc.advancements.parkourMaster.desc"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/parkour_master"));

		WORLD_RECORD_PACE = Advancement.Builder.create().parent(ROOT).display(
			PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), Potions.SWIFTNESS),
			Text.translatable("uhc.advancements.worldRecordPace"),
			Text.translatable("uhc.advancements.worldRecordPace.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/world_record_pace"));

		THATS_EMBARRASSING = Advancement.Builder.create().parent(ROOT).display(
			Items.SWEET_BERRIES,
			Text.translatable("uhc.advancements.thatsEmbarrassing"),
			Text.translatable("uhc.advancements.thatsEmbarrassing.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/thats_embarrassing"));

		BUSTED = Advancement.Builder.create().parent(ROOT).display(
			Items.BARRIER,
			Text.translatable("uhc.advancements.busted"),
			Text.translatable("uhc.advancements.busted.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/busted"));

		DEMOLITION_EXPERT = Advancement.Builder.create().parent(EARLY_EXIT).display(
			Items.TNT,
			Text.translatable("uhc.advancements.demolitionExpert"),
			Text.translatable("uhc.advancements.demolitionExpert.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/demolition_expert"));

		OK_WE_BELIEVE_YOU_NOW = Advancement.Builder.create().parent(COMBAT_LOGGER).display(
			Items.WOODEN_HOE,
			Text.translatable("uhc.advancements.okWeBelieveYouNow"),
			Text.translatable("uhc.advancements.okWeBelieveYouNow.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/ok_we_believe_you_now"));

		FALLING_BLOCK = Advancement.Builder.create().parent(THATS_NOT_DUSTLESS).display(
			Items.SAND,
			Text.translatable("uhc.advancements.fallingBlock"),
			Text.translatable("uhc.advancements.fallingBlock.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/falling_block"));

		DREAM_LUCK = Advancement.Builder.create().parent(BUSTED).display(
			Items.ENCHANTED_GOLDEN_APPLE,
			Text.translatable("uhc.advancements.dreamLuck"),
			Text.translatable("uhc.advancements.dreamLuck.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/dream_luck"));

		BROKEN_ANKLES = Advancement.Builder.create().parent(THATS_EMBARRASSING).display(
			Items.LEATHER_BOOTS,
			Text.translatable("uhc.advancements.brokenAnkles"),
			Text.translatable("uhc.advancements.brokenAnkles.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/broken_ankles"));

		ON_THE_EDGE = Advancement.Builder.create().parent(BROKEN_ANKLES).display(
			PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), Potions.STRONG_HARMING),
			Text.translatable("uhc.advancements.onTheEdge"),
			Text.translatable("uhc.advancements.onTheEdge.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/on_the_edge"));

		SKILL_ISSUE = Advancement.Builder.create().parent(EARLY_EXIT).display(
			Items.BONE,
			Text.translatable("uhc.advancements.skillIssue"),
			Text.translatable("uhc.advancements.skillIssue.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/skill_issue"));

		SOLOIST = Advancement.Builder.create().parent(ROOT).display(
			Items.PLAYER_HEAD,
			Text.translatable("uhc.advancements.soloist"),
			Text.translatable("uhc.advancements.soloist.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/soloist"));

		NOT_NOW = Advancement.Builder.create().parent(ROOT).display(
			Items.NETHERITE_SWORD,
			Text.translatable("uhc.advancements.notNow"),
			Text.translatable("uhc.advancements.notNow.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/not_now"));

		LDAP = Advancement.Builder.create().parent(NOT_NOW).display(
			Items.EMERALD_BLOCK,
			Text.translatable("uhc.advancements.ldap"),
			Text.translatable("uhc.advancements.ldap.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/ldap"));

		OFFICIALLY_BORED = Advancement.Builder.create().parent(WORLD_RECORD_PACE).display(
			Items.COMMAND_BLOCK,
			Text.translatable("uhc.advancements.officiallyBored"),
			Text.translatable("uhc.advancements.officiallyBored.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/officially_bored"));

    	DISTRACTED = Advancement.Builder.create().parent(OFFICIALLY_BORED).display(
			Items.CHAIN_COMMAND_BLOCK,
			Text.translatable("uhc.advancements.distracted"),
			Text.translatable("uhc.advancements.distracted.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/distracted"));

		UH_OH = Advancement.Builder.create().parent(LDAP).display(
			Items.BARRIER,
			Text.translatable("uhc.advancements.uhOh"),
			Text.translatable("uhc.advancements.uhOh.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/uh_oh"));

		BASICALLY = Advancement.Builder.create().parent(LDAP).display(
			Items.WHITE_WOOL,
			Text.translatable("uhc.advancements.basically"),
			Text.translatable("uhc.advancements.basically.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/basically"));

		FIND_THE_BUTTON = Advancement.Builder.create().parent(OFFICIALLY_BORED).display(
			Items.STONE_BUTTON,
			Text.translatable("uhc.advancements.findTheButton"),
			Text.translatable("uhc.advancements.findTheButton.desc"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/find_the_button"));

		Scheduler.schedule(0, MinecraftTimeUnit.Ticks, () -> {
			registerAdvancements();
			registerListeners();
		});
	}

	public static void noop() { }

	private static void registerAdvancements() {
		// Too lazy to manually register each one
		for (Field field : UHCAdvancements.class.getDeclaredFields()) {
			if (Advancement.class.isAssignableFrom(field.getType())) {
				try {
					AdvancementHandler.register((Advancement) field.get(null));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void registerListeners() {
		EventHandler.register(UHCEndEvent.class, event -> {
			AtomicReference<PlayerAttacker> lowest = new AtomicReference<>();
			AtomicReference<PlayerAttacker> highest = new AtomicReference<>();
			PlayerManager.forEveryPlayer(p -> {
				if (PlayerManager.isPlayerPlaying(p)) {
					double current = PlayerExtension.get(p).getStats().get(UHCStat.DAMAGE_DEALT);
					if (lowest.get() == null) {
						PlayerAttacker first = new PlayerAttacker(p, current);
						lowest.set(first);
						highest.set(first);
					}
					if (lowest.get().damageDealt > current) {
						lowest.set(new PlayerAttacker(p, current));
					} else if (highest.get().damageDealt < current) {
						highest.set(new PlayerAttacker(p, current));
					}
				}
			});

			if (lowest.get() != null) {
				PlayerManager.grantAdvancement(lowest.get().player, MOSTLY_HARMLESS);
				PlayerManager.grantAdvancement(highest.get().player, HEAVY_HITTER);
			}
		});

		EventHandler.register(PlayerJoinEvent.class, event -> {
			if (PlayerManager.isPlayerPlayingInSurvival(event.getPlayer())) {
				PlayerExtension extension = PlayerExtension.get(event.getPlayer());
				PlayerStats stats = extension.getStats();
				stats.increment(UHCStat.RELOGS, 1);

				// Wait for player to load in
				Scheduler.schedule(5, MinecraftTimeUnit.Seconds, () -> {
					PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.COMBAT_LOGGER);
					if (stats.get(UHCStat.RELOGS) == 10) {
						PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.OK_WE_BELIEVE_YOU_NOW);
					}
				});
			}
		});

		EventHandler.register(PlayerDeathEvent.class, event -> {
			if (event.getPlayer().currentScreenHandler instanceof MinesweeperScreen) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.DISTRACTED);
			}
		});

		EventHandler.register(PlayerBlockPlacedEvent.class, event -> {
			BlockState state = event.getState();
			Block block = state.getBlock();
			ItemPlacementContext context = event.getContext();
			BlockPos pos = context.getBlockPos();
			World world = context.getWorld();
			if (block instanceof FallingBlock && FallingBlock.canFallThrough(world.getBlockState(pos.down())) || pos.getY() < world.getBottomY()) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.FALLING_BLOCK);
			} else if (block == Blocks.REDSTONE_WIRE) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.THATS_NOT_DUSTLESS);
			} else if (block == Blocks.TNT) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.DEMOLITION_EXPERT);
			}
		});

		EventHandler.register(PlayerCraftEvent.class, event -> {
			if (event.getStack().isOf(Items.CRAFTING_TABLE) && UHCManager.isUnclaimed(OneTimeAchievement.CRAFT)) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.WORLD_RECORD_PACE);
			}
		});

		EventHandler.register(PlayerBorderDamageEvent.class, event -> {
			float damage = event.getAmount();
			if (event.getPlayer().getHealth() - damage <= 0) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.SKILL_ISSUE);
			}
		});

		EventHandler.register(PlayerLootEvent.class, event -> {
			if (event.getItems().stream().anyMatch(s -> s.isOf(Items.ENCHANTED_GOLDEN_APPLE))) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.DREAM_LUCK);
			}
		});

		EventHandler.register(PlayerTickEvent.class, event -> {
			ServerPlayerEntity player = event.getPlayer();
			PlayerExtension extension = PlayerExtension.get(event.getPlayer());
			if (PlayerManager.isPlayerPlayingInSurvival(player) && player.getHealth() <= 1.0) {
				if (extension.incrementHalfHealthTicks() == 1200) {
					PlayerManager.grantAdvancement(player, UHCAdvancements.ON_THE_EDGE);
				}
			} else {
				extension.resetHalfHealthTicks();
			}
		});

		EventHandler.register(PlayerLandEvent.class, event -> {
			if (UHCManager.isGameActive() && UHCManager.gameUptime() < 1200 && event.getDamage() > 0) {
				PlayerManager.grantAdvancement(event.getPlayer(), UHCAdvancements.BROKEN_ANKLES);
			}
		});

		EventHandler.register(PlayerChatEvent.class, event -> {
			ServerPlayerEntity player = event.getPlayer();
			String message = event.getMessage().getSignedContent().toLowerCase(Locale.ROOT);
			if (PlayerManager.isMessageGlobal(player, message)) {
				if (message.contains("jndi") && message.contains("ldap")) {
					PlayerManager.grantAdvancement(player, UHCAdvancements.LDAP);
				}
				if (message.contains("basically")) {
					PlayerManager.grantAdvancement(player, UHCAdvancements.BASICALLY);
				}
			}
		});

		EventHandler.register(PlayerBlockCollisionEvent.class, event -> {
			if (event.getState().isOf(Blocks.SWEET_BERRY_BUSH)) {
				PlayerManager.grantAdvancement(event.getEntity(), UHCAdvancements.THATS_EMBARRASSING);
			}
		});
	}

	private record PlayerAttacker(ServerPlayerEntity player, double damageDealt) { }
}
