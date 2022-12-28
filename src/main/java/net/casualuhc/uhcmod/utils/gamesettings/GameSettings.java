package net.casualuhc.uhcmod.utils.gamesettings;

import com.google.common.collect.ImmutableMap;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.uhc.ItemUtils;
import net.casualuhc.uhcmod.utils.uhc.Phase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;

import java.util.*;

public class GameSettings {
	public static final Map<ItemStack, GameSetting<?>> RULES = new LinkedHashMap<>();

	public static final GameSetting.DoubleGameSetting WORLD_BORDER_SPEED;
	public static final GameSetting.DoubleGameSetting HEALTH;
	public static final GameSetting.DoubleGameSetting BOW_COOLDOWN;

	public static final GameSetting.BooleanGameSetting END_GAME_GLOW;
	public static final GameSetting.BooleanGameSetting FRIENDLY_PLAYER_GLOW;
	public static final GameSetting.BooleanGameSetting PLAYER_DROPS_GAPPLE_ON_DEATH;
	public static final GameSetting.BooleanGameSetting FLOODGATE;
	public static final GameSetting.BooleanGameSetting DISPLAY_TAB;
	public static final GameSetting.BooleanGameSetting PVP;
	public static final GameSetting.BooleanGameSetting OP_POTIONS;
	public static final GameSetting.BooleanGameSetting PLAYER_DROPS_HEAD_ON_DEATH;
	public static final GameSetting.BooleanGameSetting GENERATE_PORTAL;
	public static final GameSetting.BooleanGameSetting MINESWEEPER_ANNOUNCEMENT;
	public static final GameSetting.BooleanGameSetting HEADS_CONSUMABLE;

	static {
		WORLD_BORDER_SPEED = new GameSetting.DoubleGameSetting(
			ItemUtils.literalNamed(Items.DIAMOND_BOOTS, "Border Speed"),
			ImmutableMap.of(
				// Why did I pick cake, I have no idea?
				ItemUtils.literalNamed(Items.CAKE, "Insane"), 1 / 40D,
				ItemUtils.literalNamed(Items.GREEN_STAINED_GLASS_PANE, "Fast"), 0.9D,
				ItemUtils.literalNamed(Items.YELLOW_STAINED_GLASS_PANE, "Normal"), 1.0D,
				ItemUtils.literalNamed(Items.RED_STAINED_GLASS_PANE, "Slow"), 1.1D
			),
			1.0D
		);

		HEALTH = new GameSetting.DoubleGameSetting(
			PotionUtil.setPotion(ItemUtils.literalNamed(Items.POTION, "Health"), Potions.HEALING),
			ImmutableMap.of(
				ItemUtils.literalNamed(Items.GREEN_STAINED_GLASS_PANE, "Triple"), 2.0D,
				ItemUtils.literalNamed(Items.YELLOW_STAINED_GLASS_PANE, "Double"), 1.0D,
				ItemUtils.literalNamed(Items.RED_STAINED_GLASS_PANE, "Normal"), 0.0D
			),
			0.0D
		);

		BOW_COOLDOWN = new GameSetting.DoubleGameSetting(
			ItemUtils.literalNamed(Items.BOW, "Bow Cooldown"),
			ImmutableMap.of(
				ItemUtils.literalNamed(Items.CLOCK, "None"), 0.0D,
				ItemUtils.literalNamed(Items.CLOCK, "0.5 Seconds"), 0.5D,
				ItemUtils.literalNamed(Items.CLOCK, "1 Second"), 1.0D,
				ItemUtils.literalNamed(Items.CLOCK, "2 Seconds"), 2.0D,
				ItemUtils.literalNamed(Items.CLOCK, "3 Seconds"), 3.0D,
				ItemUtils.literalNamed(Items.CLOCK, "5 Seconds"), 5.0D
			),
			0.0D
		);

		END_GAME_GLOW = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.SPECTRAL_ARROW, "End Game Glow"),
			getBooleanRuleOptions(),
			true
		);

		FRIENDLY_PLAYER_GLOW = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.GOLDEN_CARROT, "Friendly Player Glow"),
			getBooleanRuleOptions(),
			true
		);

		PLAYER_DROPS_GAPPLE_ON_DEATH = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.GOLDEN_APPLE, "Player Drops Gapple"),
			getBooleanRuleOptions(),
			false
		);

		FLOODGATE = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.COBBLESTONE_WALL, "Floodgates"),
			getBooleanRuleOptions(),
			false
		);

		DISPLAY_TAB = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.WHITE_STAINED_GLASS_PANE, "Display Tab Info"),
			getBooleanRuleOptions(),
			true,
			booleanSetting -> PlayerManager.displayTab = booleanSetting.getValue()
		);

		PVP = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.DIAMOND_SWORD, "Pvp"),
			getBooleanRuleOptions(),
			true,
			booleanSetting -> UHCMod.SERVER.setPvpEnabled(booleanSetting.getValue())
		);

		OP_POTIONS = new GameSetting.BooleanGameSetting(
			PotionUtil.setPotion(ItemUtils.literalNamed(Items.SPLASH_POTION, "Op Potions"), Potions.STRONG_HEALING),
			getBooleanRuleOptions(),
			false
		);

		PLAYER_DROPS_HEAD_ON_DEATH = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.PLAYER_HEAD, "Player Head Drops"),
			getBooleanRuleOptions(),
			false
		);

		GENERATE_PORTAL = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.CRYING_OBSIDIAN, "Generate Nether Portals"),
			getBooleanRuleOptions(),
			true
		);

		MINESWEEPER_ANNOUNCEMENT = new GameSetting.BooleanGameSetting(
			ItemUtils.literalNamed(Items.JUKEBOX, "Minesweeper Announcement"),
			getBooleanRuleOptions(),
			true
		);

		HEADS_CONSUMABLE = new GameSetting.BooleanGameSetting(
			ItemUtils.generateGoldenHead().setCustomName(Text.of("Consumable Heads")),
			getBooleanRuleOptions(),
			false
		);
	}

	private static Map<ItemStack, Boolean> getBooleanRuleOptions() {
		return ImmutableMap.of(
			ItemUtils.literalNamed(Items.GREEN_STAINED_GLASS_PANE, "On"), true,
			ItemUtils.literalNamed(Items.RED_STAINED_GLASS_PANE, "Off"), false
		);
	}
}
