package net.casualuhc.uhcmod.features;

import com.google.common.collect.ImmutableSet;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.UHCEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class UHCAdvancements {
	private static final Set<Advancement> UHC_ADVANCEMENTS;
	public static final Advancement ROOT;
	public static final Advancement FIRST_BLOOD;
	public static final Advancement EARLY_EXIT;
	public static final Advancement MOSTLY_HARMLESS;
	public static final Advancement HEAVY_HITTER;
	public static final Advancement WINNER;
	public static final Advancement COMBAT_LOGGER;
	public static final Advancement ON_THE_EDGE;
	public static final Advancement THATS_NOT_DUSTLESS;

	static {
		ROOT = Advancement.Builder.create().display(
			Items.GOLDEN_APPLE,
			Text.literal("UHC Advancements"),
			Text.literal("Advancements to achieve during UHC!"),
			new Identifier("textures/gui/advancements/backgrounds/adventure.png"),
			AdvancementFrame.TASK,
			false,
			false,
			false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/root"));

		FIRST_BLOOD = Advancement.Builder.create().parent(ROOT).display(
			Items.IRON_SWORD,
			Text.literal("First Blood"),
			Text.literal("Kill the first person of the game"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/first_blood"));

		EARLY_EXIT = Advancement.Builder.create().parent(ROOT).display(
			Items.POISONOUS_POTATO,
			Text.literal("Early Exit"),
			Text.literal("Someone had to go out first ¯\\_(ツ)_/¯"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/early_exit"));

		MOSTLY_HARMLESS = Advancement.Builder.create().parent(ROOT).display(
			Items.FEATHER,
			Text.literal("Mostly Harmless"),
			Text.literal("Least damage dealt to other players"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/mostly_harmless"));

		HEAVY_HITTER = Advancement.Builder.create().parent(ROOT).display(
			Items.ANVIL,
			Text.literal("Heavy Hitter"),
			Text.literal("Most damage dealt to other players"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/heavy_hitter"));

		WINNER = Advancement.Builder.create().parent(ROOT).display(
			Items.TOTEM_OF_UNDYING,
			Text.literal("WINNER!"),
			Text.literal("Congratulations on winning the UHC!"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/winner"));

		COMBAT_LOGGER = Advancement.Builder.create().parent(ROOT).display(
			Items.WOODEN_SWORD,
			Text.literal("Combat Logger"),
			Text.literal("We all believe it was your internet..."),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/combat_logger"));

		ON_THE_EDGE = Advancement.Builder.create().parent(ROOT).display(
			PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), Potions.STRONG_HARMING),
			Text.literal("Living Life On The Edge"),
			Text.literal("Surviving 60 seconds on half a heart"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/on_the_edge"));

		THATS_NOT_DUSTLESS = Advancement.Builder.create().parent(ROOT).display(
			Items.REDSTONE,
			Text.literal("That's Not Dustless!"),
			Text.literal("Use redstone dust"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/thats_not_dustless"));

		UHC_ADVANCEMENTS = ImmutableSet.of(ROOT, FIRST_BLOOD, EARLY_EXIT, MOSTLY_HARMLESS, HEAVY_HITTER, WINNER, COMBAT_LOGGER, ON_THE_EDGE, THATS_NOT_DUSTLESS);

		EventHandler.register(new UHCEvents() {
			@Override
			public void onEnd() {
				AtomicReference<PlayerAttacker> lowest = new AtomicReference<>();
				AtomicReference<PlayerAttacker> highest = new AtomicReference<>();
				PlayerUtils.forEveryPlayer(p -> {
					int current = PlayerExtension.get(p).damageDealt;
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
				});

				if (lowest.get() != null) {
					PlayerUtils.grantAdvancement(lowest.get().player, MOSTLY_HARMLESS);
					PlayerUtils.grantAdvancement(highest.get().player(), HEAVY_HITTER);
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

	private record PlayerAttacker(ServerPlayerEntity player, int damageDealt) { }
}
