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

	static {
		ROOT = Advancement.Builder.create().display(
			Items.GOLDEN_APPLE,
			Text.literal("UHC Advancements"),
			Text.literal("Advancements to achieve during UHC!"),
			new Identifier("textures/gui/advancements/backgrounds/adventure.png"),
			AdvancementFrame.TASK,
			false, false, false
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

		MOSTLY_HARMLESS = Advancement.Builder.create().parent(EARLY_EXIT).display(
			Items.FEATHER,
			Text.literal("Mostly Harmless"),
			Text.literal("Least damage dealt to other players"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/mostly_harmless"));

		HEAVY_HITTER = Advancement.Builder.create().parent(FIRST_BLOOD).display(
			Items.ANVIL,
			Text.literal("Heavy Hitter"),
			Text.literal("Most damage dealt to other players"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/heavy_hitter"));

		WINNER = Advancement.Builder.create().parent(FIRST_BLOOD).display(
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

		THATS_NOT_DUSTLESS = Advancement.Builder.create().parent(ROOT).display(
			Items.REDSTONE,
			Text.literal("That's Not Dustless!"),
			Text.literal("Use redstone dust"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/thats_not_dustless"));

		PARKOUR_MASTER = Advancement.Builder.create().parent(ROOT).display(
			Items.NETHERITE_BOOTS,
			Text.literal("Parkour Master"),
			Text.literal("Complete the parkour in the lobby"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/parkour_master"));

		WORLD_RECORD_PACE = Advancement.Builder.create().parent(ROOT).display(
			PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), Potions.SWIFTNESS),
			Text.literal("World Record Pace!"),
			Text.literal("Be the first to craft a crafting table"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/world_record_pace"));

		THATS_EMBARRASSING = Advancement.Builder.create().parent(ROOT).display(
			Items.SWEET_BERRIES,
			Text.literal("That's Embarrassing"),
			Text.literal("Take damage from a sweet berry bush"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/thats_embarrassing"));

		BUSTED = Advancement.Builder.create().parent(ROOT).display(
			Items.BARRIER,
			Text.literal("Busted"),
			Text.literal("Break the rules by using flexible block placement"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/busted"));

		DEMOLITION_EXPERT = Advancement.Builder.create().parent(EARLY_EXIT).display(
			Items.TNT,
			Text.literal("Demolitions Expert"),
			Text.literal("Are you sure you know what you're doing with TNT?"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/demolition_expert"));

		OK_WE_BELIEVE_YOU_NOW = Advancement.Builder.create().parent(COMBAT_LOGGER).display(
			Items.WOODEN_HOE,
			Text.literal("Ok We Believe You Now"),
			Text.literal("Should've used KCP"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/ok_we_believe_you_now"));

		FALLING_BLOCK = Advancement.Builder.create().parent(THATS_NOT_DUSTLESS).display(
			Items.SAND,
			Text.literal("Falling Block"),
			Text.literal("Place a falling block"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/falling_block"));

		DREAM_LUCK = Advancement.Builder.create().parent(BUSTED).display(
			Items.ENCHANTED_GOLDEN_APPLE,
			Text.literal("Dream Luck"),
			Text.literal("Find an enchanted golden apple"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/dream_luck"));

		BROKEN_ANKLES = Advancement.Builder.create().parent(THATS_EMBARRASSING).display(
			Items.LEATHER_BOOTS,
			Text.literal("Broken Ankles"),
			Text.literal("Take fall damage within a minute of UHC starting"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/broken_ankles"));

		ON_THE_EDGE = Advancement.Builder.create().parent(BROKEN_ANKLES).display(
			PotionUtil.setPotion(Items.SPLASH_POTION.getDefaultStack(), Potions.STRONG_HARMING),
			Text.literal("Living Life On The Edge"),
			Text.literal("Surviving 60 seconds on half a heart"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/on_the_edge"));

		SKILL_ISSUE = Advancement.Builder.create().parent(EARLY_EXIT).display(
			Items.BONE,
			Text.literal("Skill Issue"),
			Text.literal("Die to the world border"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/skill_issue"));

		SOLOIST = Advancement.Builder.create().parent(ROOT).display(
			Items.PLAYER_HEAD,
			Text.literal("Soloist"),
			Text.literal("Teammates are overrated"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/soloist"));

		NOT_NOW = Advancement.Builder.create().parent(ROOT).display(
			Items.NETHERITE_SWORD,
			Text.literal("Not Now"),
			Text.literal("Sensei's words echoed... There's a time and a place for everything, but not now."),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/not_now"));

		LDAP = Advancement.Builder.create().parent(NOT_NOW).display(
			Items.EMERALD_BLOCK,
			Text.literal("Ldap"),
			Text.literal("Hi Katie <3"),
			null,
			AdvancementFrame.TASK,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/ldap"));

		OFFICIALLY_BORED = Advancement.Builder.create().parent(WORLD_RECORD_PACE).display(
			Items.COMMAND_BLOCK,
			Text.literal("Officially Bored"),
			Text.literal("Beat minesweeper in less than 40 seconds"),
			null,
			AdvancementFrame.CHALLENGE,
			true, true, false
		).criterion("impossible", new ImpossibleCriterion.Conditions()).build(new Identifier("uhc/officially_bored"));

		UHC_ADVANCEMENTS = ImmutableSet.of(
			ROOT,
			FIRST_BLOOD,
			EARLY_EXIT,
			MOSTLY_HARMLESS,
			HEAVY_HITTER,
			WINNER,
			COMBAT_LOGGER,
			ON_THE_EDGE,
			THATS_NOT_DUSTLESS,
			PARKOUR_MASTER,
			WORLD_RECORD_PACE,
			THATS_EMBARRASSING,
			BUSTED,
			DEMOLITION_EXPERT,
			OK_WE_BELIEVE_YOU_NOW,
			FALLING_BLOCK,
			DREAM_LUCK,
			BROKEN_ANKLES,
			SKILL_ISSUE,
			SOLOIST,
			LDAP,
			NOT_NOW,
			OFFICIALLY_BORED
		);

		EventHandler.register(new UHCEvents() {
			@Override
			public void onEnd() {
				AtomicReference<PlayerAttacker> lowest = new AtomicReference<>();
				AtomicReference<PlayerAttacker> highest = new AtomicReference<>();
				PlayerUtils.forEveryPlayer(p -> {
					if (PlayerUtils.isPlayerPlaying(p)) {
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
					}
				});

				if (lowest.get() != null) {
					PlayerUtils.grantAdvancement(lowest.get().player, MOSTLY_HARMLESS);
					PlayerUtils.grantAdvancement(highest.get().player, HEAVY_HITTER);
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
