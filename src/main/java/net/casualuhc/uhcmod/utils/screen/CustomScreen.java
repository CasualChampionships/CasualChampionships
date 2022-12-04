package net.casualuhc.uhcmod.utils.screen;

import net.casualuhc.uhcmod.utils.Scheduler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.function.Consumer;

public abstract class CustomScreen extends ScreenHandler {;
	private final PlayerInventory playerInventory;
	private final Inventory inventory;

	/**
	 * Creates a custom screen with a given player inventory.
	 */
	public CustomScreen(PlayerInventory playerInventory, int syncId, int rows) {
		super(rowsToType(rows), syncId);
		this.playerInventory = playerInventory;
		this.inventory = new SimpleInventory(9 * rows);

		int i = (rows - 4) * 18;

		for (int j = 0; j < rows; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(this.inventory, k + j * 9, 8 + k * 18, 18 + j * 18));
			}
		}

		for (int j = 0; j < 3; j++) {
			for (int k = 0; k < 9; k++) {
				this.addSlot(new Slot(playerInventory, k + j * 9 + 9, 8 + k * 18, 103 + j * 18 + i));
			}
		}

		for (int j = 0; j < 9; j++) {
			this.addSlot(new Slot(playerInventory, j, 8 + j * 18, 161 + i));
		}
	}

	/**
	 * Creates a custom screen with an empty player inventory.
	 */
	public CustomScreen(PlayerEntity player, int syncId, int rows) {
		this(new PlayerInventory(player), syncId, rows);
	}

	public void modifyInventory(Consumer<Inventory> inventoryModifier) {
		inventoryModifier.accept(this.inventory);
	}

	public void modifyPlayerInventory(Consumer<Inventory> inventoryModifier) {
		inventoryModifier.accept(this.playerInventory);
	}


	@Override
	public abstract void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player);

	@Override
	public final ItemStack transferSlot(PlayerEntity player, int index) {
		throw new UnsupportedOperationException("Cannot transfer slots from within a CustomScreen");
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return true;
	}

	@Override
	public void close(PlayerEntity player) {
		super.close(player);
		player.playerScreenHandler.syncState();
		// Scheduler.schedule(0, player.playerScreenHandler::syncState);
	}

	private static ScreenHandlerType<?> rowsToType(int rows) {
		return switch (rows) {
			case 1 -> ScreenHandlerType.GENERIC_9X1;
			case 2 -> ScreenHandlerType.GENERIC_9X2;
			case 3 -> ScreenHandlerType.GENERIC_9X3;
			case 4 -> ScreenHandlerType.GENERIC_9X4;
			case 5 -> ScreenHandlerType.GENERIC_9X5;
			case 6 -> ScreenHandlerType.GENERIC_9X6;
			default -> throw new IllegalStateException("Invalid number of rows: " + rows);
		};
	}
}
