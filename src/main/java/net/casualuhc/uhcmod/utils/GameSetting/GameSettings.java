package net.casualuhc.uhcmod.utils.GameSetting;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.casualuhc.uhcmod.managers.WorldBorderManager.Stage;
import net.casualuhc.uhcmod.utils.GameSetting.GameSetting.NamedItemStack;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GameSettings {
	public static final Map<String, GameSetting<?>> gameSettingMap = new LinkedHashMap<>();

	public static final GameSetting.DoubleGameSetting WORLD_BORDER_SPEED, HEALTH;
	public static final GameSetting.BooleanGameSetting END_GAME_GLOW, FRIENDLY_PLAYER_GLOW, PLAYER_DROPS_GAPPLE_ON_DEATH, FLOODGATE, DISPLAY_TAB, PVP, OP_POTIONS, BOW_COOLDOWN, PLAYER_DROPS_HEAD_ON_DEATH;
	public static final GameSetting.EnumGameSetting<Stage> WORLD_BORDER_STAGE;

	static {
		WORLD_BORDER_SPEED = new GameSetting.DoubleGameSetting(
			NamedItemStack.of("border_speed", Items.DIAMOND_BOOTS),
			new LinkedHashMap<>() {{
				this.put(NamedItemStack.of("insane", Items.CAKE), 1 / 40D);
				this.put(NamedItemStack.of("fast", Items.GREEN_STAINED_GLASS_PANE), 0.9D);
				this.put(NamedItemStack.of("normal", Items.YELLOW_STAINED_GLASS_PANE), 1.0D);
				this.put(NamedItemStack.of("slow", Items.RED_STAINED_GLASS_PANE), 1.1D);
			}},
			1.0D
		);

		HEALTH = new GameSetting.DoubleGameSetting(
			NamedItemStack.of("health", Items.POTION),
			new LinkedHashMap<>() {{
				this.put(NamedItemStack.of("triple", Items.GREEN_STAINED_GLASS_PANE), 2.0D);
				this.put(NamedItemStack.of("double", Items.YELLOW_STAINED_GLASS_PANE), 1.0D);
				this.put(NamedItemStack.of("normal", Items.RED_STAINED_GLASS_PANE), 0.0D);
			}},
			1.0D
		);

		Supplier<Map<NamedItemStack, Boolean>> onOffMap = () -> new LinkedHashMap<>() {{
			this.put(NamedItemStack.of("on", Items.GREEN_STAINED_GLASS_PANE), true);
			this.put(NamedItemStack.of("off", Items.RED_STAINED_GLASS_PANE), false);
		}};

		END_GAME_GLOW = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("end_game_glow", Items.SPECTRAL_ARROW),
			onOffMap.get(),
			true
		);

		FRIENDLY_PLAYER_GLOW = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("friendly_player_glow", Items.GOLDEN_CARROT),
			onOffMap.get(),
			true
		);

		PLAYER_DROPS_GAPPLE_ON_DEATH = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("player_drops_gapple_on_death", Items.GOLDEN_APPLE),
			onOffMap.get(),
			false
		);

		FLOODGATE = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("floodgates", Items.COBBLESTONE_WALL),
			onOffMap.get(),
			false,
			booleanSetting -> {
				GameManager.INSTANCE.setFloodgates(booleanSetting.getValue());
			}
		);

		DISPLAY_TAB = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("display_tab", Items.WHITE_STAINED_GLASS_PANE),
			onOffMap.get(),
			true,
			booleanSetting -> {
				PlayerUtils.displayTab = booleanSetting.getValue();
			}
		);

		PVP = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("pvp", Items.DIAMOND_SWORD),
			onOffMap.get(),
			false,
			booleanSetting -> {
				UHCMod.UHC_SERVER.setPvpEnabled(booleanSetting.getValue());
			}
		);

		OP_POTIONS = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("op_potions", Items.SPLASH_POTION),
			onOffMap.get(),
			false
		);

		BOW_COOLDOWN = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("bow_cooldown", Items.BOW),
			onOffMap.get(),
			true
		);

		PLAYER_DROPS_HEAD_ON_DEATH = new GameSetting.BooleanGameSetting(
			NamedItemStack.of("player_head_drops", Items.PLAYER_HEAD),
			onOffMap.get(),
			true
		);

		WORLD_BORDER_STAGE = new GameSetting.EnumGameSetting<>(
			NamedItemStack.of("world_border_stage", Items.BARRIER),
			new LinkedHashMap<>() {{
				boolean red = true;
				for (Stage stage : Stage.values()) {
					this.put(
						NamedItemStack.of(stage.name().toLowerCase(), red ? Items.RED_STAINED_GLASS_PANE : Items.WHITE_STAINED_GLASS_PANE), stage
					);
					red = !red;
				}
			}},
			Stage.FIRST,
			stageSetting -> {
				if (GameManager.INSTANCE.isPhase(Phase.ACTIVE)) {
					WorldBorderManager.moveWorldBorders(stageSetting.getValue().getStartSize(), 0);
					WorldBorderManager.startWorldBorders();
					return;
				}
				UHCMod.UHCLogger.error("Could not set World border since game is not active");
			}
		);
	}

	public static SimpleNamedScreenHandlerFactory createScreenFactory(int page) {
		if (page < 0) {
			return null;
		}

		SimpleInventory inventory = new SimpleInventory(54);
		List<GameSetting<?>> gameSettings = gameSettingMap.values().stream().skip(5L * page).toList();

		for (int i = 1; i <= 5; i++) {
			if (gameSettings.size() <= (i - 1)) {
				break;
			}
			int startSlot = i * 9;
			GameSetting<?> gameSetting = gameSettings.get(i - 1);
			inventory.setStack(startSlot, gameSetting.getStack());

			startSlot += 2;
			List<ItemStack> itemStacks = gameSetting.getOptions().keySet().stream().limit(7).map(named -> named.stack).toList();
			int options = itemStacks.size();
			for (int j = 0; j < options; j++) {
				inventory.setStack(startSlot + j, itemStacks.get(j));
			}
		}

		for (int i = 1; i < 8; i++) {
			inventory.setStack(i, Items.GRAY_STAINED_GLASS.getDefaultStack().setCustomName(Text.literal("")));
		}

		inventory.setStack(0, Items.RED_STAINED_GLASS.getDefaultStack().setCustomName(Text.literal("Previous")));
		inventory.setStack(8, Items.GREEN_STAINED_GLASS.getDefaultStack().setCustomName(Text.literal("Next")));

		return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> {
			return new FakeScreen(syncId, inv, inventory, page);
		}, Text.literal("Config Screen"));
	}

	public static class FakeScreen extends GenericContainerScreenHandler {
		private final int page;

		public FakeScreen(int syncId, PlayerInventory playerInventory, Inventory inventory, int page) {
			super(ScreenHandlerType.GENERIC_9X6, syncId, playerInventory, inventory, 6);
			this.page = page;
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

			String ruleName = ruleStack.getName().getString().toLowerCase().replace(" ", "_");
			String optionName = clickedStack.getName().getString().toLowerCase().replace(" ", "_");
			GameSetting<?> setting = gameSettingMap.get(ruleName);
			if (setting != null) {
				setting.setValueFromOption(optionName);
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
