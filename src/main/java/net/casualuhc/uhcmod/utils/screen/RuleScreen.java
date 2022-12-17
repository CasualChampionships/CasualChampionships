package net.casualuhc.uhcmod.utils.screen;

import net.casualuhc.uhcmod.utils.gamesettings.GameSetting;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.uhc.ItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.List;

public class RuleScreen extends CustomScreen {
	private final int page;

	public RuleScreen(PlayerInventory playerInventory, int syncId, int page) {
		super(playerInventory, syncId, 6);
		this.page = page;

		this.modifyInventory(inventory -> {
			List<GameSetting<?>> gameSettings = GameSettings.RULES.values().stream().skip(5L * this.page).toList();

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

			inventory.setStack(0, ItemUtils.literalNamed(Items.RED_STAINED_GLASS, "Previous"));
			inventory.setStack(8, ItemUtils.literalNamed(Items.GREEN_STAINED_GLASS, "Next"));
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

		GameSetting<?> setting = GameSettings.RULES.get(ruleStack);
		if (setting != null) {
			setting.setValueFromOption(clickedStack);
			Text message = ruleStack.getName().copy().append(" has been set to ").append(clickedStack.getName());
			player.sendMessage(message, false);
		}
	}

	public static SimpleNamedScreenHandlerFactory createScreenFactory(int page) {
		if (page < 0) {
			return null;
		}
		return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> {
			return new RuleScreen(inv, syncId, page);
		}, Text.literal("Rule Config Screen"));
	}
}
