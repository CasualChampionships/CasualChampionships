package net.casualuhc.uhcmod.utils.gamesettings;

import com.google.common.collect.ImmutableMap;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.casualuhc.uhcmod.managers.WorldBorderManager.Stage;
import net.casualuhc.uhcmod.utils.ItemUtil;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.screen.CustomScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	public static final GameSetting.BooleanGameSetting TESTING;
	public static final GameSetting.BooleanGameSetting MINESWEEPER_ANNOUNCEMENT;

	public static final GameSetting.EnumGameSetting<Stage> WORLD_BORDER_STAGE;

	static {
		WORLD_BORDER_SPEED = new GameSetting.DoubleGameSetting(
			ItemUtil.named(Items.DIAMOND_BOOTS, "Border Speed"),
			ImmutableMap.of(
				// Why did I pick cake, I have no idea?
				ItemUtil.named(Items.CAKE, "Insane"), 1 / 40D,
				ItemUtil.named(Items.GREEN_STAINED_GLASS_PANE, "Fast"), 0.9D,
				ItemUtil.named(Items.YELLOW_STAINED_GLASS_PANE, "Normal"), 1.0D,
				ItemUtil.named(Items.RED_STAINED_GLASS_PANE, "Slow"), 1.1D
			),
			1.0D
		);

		HEALTH = new GameSetting.DoubleGameSetting(
			PotionUtil.setPotion(ItemUtil.named(Items.POTION, "Health"), Potions.HEALING),
			ImmutableMap.of(
				ItemUtil.named(Items.GREEN_STAINED_GLASS_PANE, "Triple"), 2.0D,
				ItemUtil.named(Items.YELLOW_STAINED_GLASS_PANE, "double"), 1.0D,
				ItemUtil.named(Items.RED_STAINED_GLASS_PANE, "Normal"), 0.0D
			),
			1.0D
		);

		BOW_COOLDOWN = new GameSetting.DoubleGameSetting(
			ItemUtil.named(Items.BOW, "Bow Cooldown"),
			ImmutableMap.of(
				ItemUtil.named(Items.CLOCK, "None"), 0.0D,
				ItemUtil.named(Items.CLOCK, "0.5 Seconds"), 0.5D,
				ItemUtil.named(Items.CLOCK, "1 Second"), 1.0D,
				ItemUtil.named(Items.CLOCK, "2 Seconds"), 2.0D,
				ItemUtil.named(Items.CLOCK, "3 Seconds"), 3.0D,
				ItemUtil.named(Items.CLOCK, "5 Seconds"), 5.0D
			),
			1.0D
		);

		END_GAME_GLOW = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.SPECTRAL_ARROW, "End Game Glow"),
			getBooleanRuleOptions(),
			true
		);

		FRIENDLY_PLAYER_GLOW = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.GOLDEN_CARROT, "Friendly Player Glow"),
			getBooleanRuleOptions(),
			true
		);

		PLAYER_DROPS_GAPPLE_ON_DEATH = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.GOLDEN_APPLE, "Player Drops Gapple"),
			getBooleanRuleOptions(),
			false
		);

		FLOODGATE = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.COBBLESTONE_WALL, "Floodgates"),
			getBooleanRuleOptions(),
			false
		);

		DISPLAY_TAB = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.WHITE_STAINED_GLASS_PANE, "Display Tab Info"),
			getBooleanRuleOptions(),
			true,
			booleanSetting -> PlayerUtils.displayTab = booleanSetting.getValue()
		);

		PVP = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.DIAMOND_SWORD, "Pvp"),
			getBooleanRuleOptions(),
			false,
			booleanSetting -> UHCMod.SERVER.setPvpEnabled(booleanSetting.getValue())
		);

		OP_POTIONS = new GameSetting.BooleanGameSetting(
			PotionUtil.setPotion(ItemUtil.named(Items.SPLASH_POTION, "Op Potions"), Potions.STRONG_HEALING),
			getBooleanRuleOptions(),
			false
		);

		PLAYER_DROPS_HEAD_ON_DEATH = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.PLAYER_HEAD, "Player Head Drops"),
			getBooleanRuleOptions(),
			true
		);

		GENERATE_PORTAL = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.CRYING_OBSIDIAN, "Generate Nether Portals"),
			getBooleanRuleOptions(),
			true
		);

		TESTING = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.REDSTONE_BLOCK, "Testing"),
			getBooleanRuleOptions(),
			false
		);

		MINESWEEPER_ANNOUNCEMENT = new GameSetting.BooleanGameSetting(
			ItemUtil.named(Items.JUKEBOX, "Minesweeper Announcement"),
			getBooleanRuleOptions(),
			true
		);

		WORLD_BORDER_STAGE = new GameSetting.EnumGameSetting<>(
			ItemUtil.named(Items.BARRIER, "World Border Stage"),
			getEnumOptions(Stage.class),
			Stage.FIRST,
			stageSetting -> {
				if (GameManager.isPhase(Phase.ACTIVE)) {
					WorldBorderManager.moveWorldBorders(stageSetting.getValue().getStartSize(), 0);
					WorldBorderManager.startWorldBorders();
					return;
				}
				UHCMod.LOGGER.error("Could not set World border since game is not active");
			}
		);
	}

	public static SimpleNamedScreenHandlerFactory createScreenFactory(int page) {
		if (page < 0) {
			return null;
		}
		return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> {
			return new RuleScreenHandler(inv, syncId, page);
		}, Text.literal("Rule Config Screen"));
	}

	private static Map<ItemStack, Boolean> getBooleanRuleOptions() {
		return ImmutableMap.of(
			ItemUtil.named(Items.GREEN_STAINED_GLASS_PANE, "On"), true,
			ItemUtil.named(Items.RED_STAINED_GLASS_PANE, "Off"), false
		);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T extends Enum<T>> Map<ItemStack, T> getEnumOptions(Class<T> enumClass) {
		// This needs to be ordered
		Map<ItemStack, T> map = new LinkedHashMap<>();
		boolean isPurple = true;
		for (T constant : enumClass.getEnumConstants()) {
			String cleanedName = constant.name().toLowerCase().replaceAll("_", " ");
			Item colour = isPurple ? Items.PURPLE_STAINED_GLASS_PANE : Items.WHITE_STAINED_GLASS_PANE;
			map.put(ItemUtil.named(colour, cleanedName), constant);
			isPurple = !isPurple;
		}
		return map;
	}

	private static class RuleScreenHandler extends CustomScreen {
		private final int page;

		public RuleScreenHandler(PlayerInventory playerInventory, int syncId, int page) {
			super(playerInventory, syncId, 6);
			this.page = page;

			this.modifyInventory(inventory -> {
				List<GameSetting<?>> gameSettings = RULES.values().stream().skip(5L * this.page).toList();

				for (int i = 1; i <= 5; i++) {
					if (gameSettings.size() <= (i - 1)) {
						break;
					}
					int startSlot = i * 9;
					GameSetting<?> gameSetting = gameSettings.get(i - 1);
					inventory.setStack(startSlot, gameSetting.getDisplay());

					startSlot += 2;
					List<ItemStack> itemStacks = gameSetting.getOptions().keySet().stream().limit(7).toList();
					int options = itemStacks.size();
					for (int j = 0; j < options; j++) {
						inventory.setStack(startSlot + j, itemStacks.get(j));
					}
				}

				for (int i = 1; i < 8; i++) {
					inventory.setStack(i, Items.GRAY_STAINED_GLASS.getDefaultStack().setCustomName(Text.literal("")));
				}

				inventory.setStack(0, ItemUtil.named(Items.RED_STAINED_GLASS, "Previous"));
				inventory.setStack(8, ItemUtil.named(Items.GREEN_STAINED_GLASS, "Next"));
			});
		}

		@Override
		public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
			if (slotIndex == 0 && this.page > 0) {
				player.openHandledScreen(createScreenFactory(this.page - 1));
				return;
			}
			if (slotIndex == 8) {
				player.openHandledScreen(createScreenFactory(this.page + 1));
				return;
			}
			if (slotIndex < 9 || slotIndex > 53 || slotIndex % 9 == 0) {
				return;
			}
			int row = Math.floorDiv(slotIndex, 9);
			ItemStack ruleStack = this.slots.get(row * 9).getStack();
			ItemStack clickedStack = this.slots.get(slotIndex).getStack();

			if (clickedStack.isEmpty()) {
				return;
			}

			GameSetting<?> setting = RULES.get(ruleStack);
			if (setting != null) {
				setting.setValueFromOption(clickedStack);
				Text message = ruleStack.getName().copy().append(" has been set to ").append(clickedStack.getName());
				player.sendMessage(message, false);
			}
		}

		@Override
		public boolean canUse(PlayerEntity player) {
			return true;
		}
	}
}
