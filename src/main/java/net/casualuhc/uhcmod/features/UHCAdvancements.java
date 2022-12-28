package net.casualuhc.uhcmod.features;

import com.google.common.collect.ImmutableSet;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.casualuhc.uhcmod.utils.scheduling.Scheduler;
import net.casualuhc.uhcmod.utils.screen.MinesweeperScreen;
import net.casualuhc.uhcmod.utils.stat.PlayerStats;
import net.casualuhc.uhcmod.utils.stat.UHCStat;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Class that hard codes all the advancements for UHC.
 * <p>
 * Because I don't datapack :).
 */
public class UHCAdvancements {
	private static final Set<Advancement> UHC_ADVANCEMENTS;
	public static final Advancement ROOT;
	public static final Advancement MOSTLY_HARMLESS;
	public static final Advancement HEAVY_HITTER;
	public static final Advancement WINNER;
	public static final Advancement COMBAT_LOGGER;
	public static final Advancement ON_THE_EDGE;
	public static final Advancement BUSTED;
	public static final Advancement OK_WE_BELIEVE_YOU_NOW;
	public static final Advancement LDAP;
	public static final Advancement NOT_NOW;
	public static final Advancement OFFICIALLY_BORED;
	public static final Advancement DISTRACTED;

	static {
		ROOT = Advancement.Builder.create().display(
			Items.GOLDEN_APPLE,
			Text.translatable("uhc.advancements.root"),
			Text.translatable("uhc.advancements.root.desc"),
			new Identifier("textures/gui/advancements/backgrounds/adventure.png"),
			AdvancementFrame.TASK,
			false, false, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/root"));

		MOSTLY_HARMLESS = Advancement.Builder.create().parent(ROOT).display(
			Items.FEATHER,
			Text.translatable("uhc.advancements.mostlyHarmless"),
			Text.translatable("uhc.advancements.mostlyHarmless.desc"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/mostly_harmless"));

		HEAVY_HITTER = Advancement.Builder.create().parent(ROOT).display(
			Items.ANVIL,
			Text.translatable("uhc.advancements.heavyHitter"),
			Text.translatable("uhc.advancements.heavyHitter.desc"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/heavy_hitter"));

		WINNER = Advancement.Builder.create().parent(ROOT).display(
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

		BUSTED = Advancement.Builder.create().parent(ROOT).display(
			Items.BARRIER,
			Text.translatable("uhc.advancements.busted"),
			Text.translatable("uhc.advancements.busted.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/busted"));

		OK_WE_BELIEVE_YOU_NOW = Advancement.Builder.create().parent(COMBAT_LOGGER).display(
			Items.WOODEN_HOE,
			Text.translatable("uhc.advancements.okWeBelieveYouNow"),
			Text.translatable("uhc.advancements.okWeBelieveYouNow.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/ok_we_believe_you_now"));

		ON_THE_EDGE = Advancement.Builder.create().parent(ROOT).display(
			PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), Potions.STRONG_HARMING),
			Text.translatable("uhc.advancements.onTheEdge"),
			Text.translatable("uhc.advancements.onTheEdge.desc"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/on_the_edge"));

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

		OFFICIALLY_BORED = Advancement.Builder.create().parent(ROOT).display(
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

		UHC_ADVANCEMENTS = ImmutableSet.of(
			ROOT,
			MOSTLY_HARMLESS,
			HEAVY_HITTER,
			WINNER,
			COMBAT_LOGGER,
			ON_THE_EDGE,
			BUSTED,
			OK_WE_BELIEVE_YOU_NOW,
			LDAP,
			NOT_NOW,
			OFFICIALLY_BORED,
      		DISTRACTED
		);

		EventHandler.register(new UHCEvents() {
			@Override
			public void onEnd() {
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
			}
		});

		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onPlayerJoin(ServerPlayerEntity player) {
				if (PlayerManager.isPlayerPlayingInSurvival(player)) {
					PlayerExtension extension = PlayerExtension.get(player);
					PlayerStats stats = extension.getStats();
					stats.increment(UHCStat.RELOGS, 1);

					// Wait for player to load in
					Scheduler.schedule(Scheduler.secondsToTicks(5), () -> {
						PlayerManager.grantAdvancement(player, UHCAdvancements.COMBAT_LOGGER);
						if (stats.get(UHCStat.RELOGS) == 10) {
							PlayerManager.grantAdvancement(player, UHCAdvancements.OK_WE_BELIEVE_YOU_NOW);
						}
					});
				}
			}

			@Override
			public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
				if (player.currentScreenHandler instanceof MinesweeperScreen) {
					PlayerManager.grantAdvancement(player, UHCAdvancements.DISTRACTED);
				}
			}
		});
	}

	public static void noop() { }

	public static boolean isUhcAdvancement(Advancement advancement) {
		return UHC_ADVANCEMENTS.contains(advancement);
	}

	public static void forEachAdvancement(Consumer<Advancement> consumer) {
		UHC_ADVANCEMENTS.forEach(consumer);
	}

	private record PlayerAttacker(ServerPlayerEntity player, double damageDealt) { }
}
