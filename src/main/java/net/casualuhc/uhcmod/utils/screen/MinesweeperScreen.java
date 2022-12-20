package net.casualuhc.uhcmod.utils.screen;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.utils.data.PlayerExtension;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.casualuhc.uhcmod.utils.event.MinecraftEvents;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.stat.PlayerStats;
import net.casualuhc.uhcmod.utils.stat.UHCStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

import static net.casualuhc.uhcmod.utils.uhc.ItemUtils.literalNamed;
import static net.casualuhc.uhcmod.utils.uhc.ItemUtils.translatableNamed;

public class MinesweeperScreen extends CustomScreen {
	private static double record = 127;

	private final IntSet guessed = new IntArraySet();
	private final IntSet flags = new IntArraySet();
	private final Grid grid = new Grid(9, 9);

	private final ItemStack flagItem = translatableNamed(Items.MANGROVE_SIGN, "uhc.minesweeper.flags");
	private final ItemStack clockItem = translatableNamed(Items.CLOCK, "uhc.minesweeper.timer");

	private boolean complete = false;

	public MinesweeperScreen(PlayerEntity player, int syncId) {
		super(player, syncId, 6);
		this.flagItem.setCount(12);

		this.slots.get(81).setStack(translatableNamed(Items.RED_STAINED_GLASS, "uhc.minesweeper.exit"));
		this.slots.get(82).setStack(translatableNamed(Items.OAK_SIGN, "uhc.minesweeper.desc.1"));
		this.slots.get(83).setStack(translatableNamed(Items.OAK_SIGN, "uhc.minesweeper.desc.2"));
		this.slots.get(84).setStack(translatableNamed(Items.OAK_SIGN, "uhc.minesweeper.desc.3"));
		this.slots.get(85).setStack(translatableNamed(Items.OAK_SIGN, "uhc.minesweeper.desc.4"));
		this.slots.get(86).setStack(this.flagItem);
		this.slots.get(87).setStack(this.clockItem);
		this.slots.get(88).setStack(literalNamed(Items.GRAY_STAINED_GLASS, ""));
		this.slots.get(89).setStack(translatableNamed(Items.GREEN_STAINED_GLASS, "uhc.minesweeper.playAgain"));

		EventHandler.register(new MinecraftEvents() {
			@Override
			public void onServerTick(MinecraftServer server) {
				if (MinesweeperScreen.this.grid.startTime != 0 && !MinesweeperScreen.this.complete) {
					int seconds = (int) Math.floor((System.nanoTime() - MinesweeperScreen.this.grid.startTime) / 1_000_000_000D);
					MinesweeperScreen.this.clockItem.setCount(MathHelper.clamp(seconds, 1, 127));
				}
			}
		});
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		if (slotIndex >= 0 && slotIndex < this.grid.capacity) {
			if (actionType != SlotActionType.PICKUP || this.complete) {
				return;
			}
			if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && !this.flags.contains(slotIndex)) {
				this.leftClickTile(slotIndex, player);
			}
			if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				this.rightClickTile(slotIndex);
			}
		}
		if (slotIndex == 81) {
			((ServerPlayerEntity) player).closeHandledScreen();
		}
		if (slotIndex == 89) {
			player.openHandledScreen(createScreenFactory());
		}
	}

	private void leftClickTile(int index, PlayerEntity player) {
		this.grid.checkGenerated(index);
		this.recursiveClickTile(index, player, null);
	}

	private void recursiveClickTile(int index, PlayerEntity player, IntSet checked) {
		int tile = this.grid.getTile(index);
		if (tile == -1) {
			this.onLose(player);
			return;
		}
		if (tile == Integer.MIN_VALUE) {
			return;
		}
		this.guessed.add(index);
		ItemStack stack = this.getTileStack(tile);
		this.slots.get(index).setStack(stack);
		this.checkWin(player);
		if (tile == 0) {
			if (checked == null) {
				checked = new IntArraySet();
			}
			checked.add(index);
			for (int surrounding : this.grid.getSurroundingIndices(index)) {
				if (this.flags.contains(surrounding)) {
					this.rightClickTile(surrounding);
				}
				if (checked.add(surrounding)) {
					this.recursiveClickTile(surrounding, player, checked);
				}
			}
		}
	}

	private void rightClickTile(int index) {
		if (this.slots.get(index).hasStack() && !this.flags.contains(index)) {
			return;
		}
		ItemStack stack;
		if (this.flags.contains(index)) {
			this.flags.remove(index);
			stack = ItemStack.EMPTY;
		} else {
			this.flags.add(index);
			stack = translatableNamed(Items.MANGROVE_SIGN, "uhc.minesweeper.flag");
		}
		this.flagItem.setCount(Math.max(12 - this.flags.size(), 1));
		this.slots.get(index).setStack(stack);
	}

	private void checkWin(PlayerEntity player) {
		int possibleGuesses = this.grid.capacity - this.grid.getMineCount();
		if (this.guessed.size() == possibleGuesses) {
			this.onWin(player);
		}
	}

	private void onWin(PlayerEntity player) {
		this.complete = true;
		double seconds = (System.nanoTime() - this.grid.startTime) / 1_000_000_000D;
		if (seconds <= 40) {
			PlayerManager.grantAdvancement((ServerPlayerEntity) player, UHCAdvancements.OFFICIALLY_BORED);
		}

		player.sendMessage(Text.translatable("uhc.minesweeper.won", String.format("%.2f", seconds)));
		if (seconds < record && GameSettings.MINESWEEPER_ANNOUNCEMENT.getValue()) {
			record = seconds;
			PlayerManager.messageEveryPlayer(
				Text.translatable("uhc.minesweeper.record", player.getEntityName(), String.format("%.2f", seconds))
			);
		}
		PlayerStats stats = PlayerExtension.get(player).getStats();
		double current = stats.get(UHCStat.MINESWEEPER_RECORD);
		if (Double.isNaN(current) || current > seconds) {
			stats.set(UHCStat.MINESWEEPER_RECORD, seconds);
		}
	}

	private void onLose(PlayerEntity player) {
		this.complete = true;
		for (int i = 0; i < this.grid.capacity; i++) {
			this.slots.get(i).setStack(this.getTileStack(this.grid.getTile(i)));
		}
		player.sendMessage(Text.translatable("uhc.minesweeper.lost"));
	}

	private ItemStack getTileStack(int tile) {
		return switch (tile) {
			case -1 -> translatableNamed(Items.BLACK_STAINED_GLASS_PANE, "uhc.minesweeper.mine");
			case 0 -> literalNamed(Items.GRAY_STAINED_GLASS_PANE, "");
			case 1 -> literalNamed(Items.BLUE_STAINED_GLASS_PANE, "1");
			case 2 -> literalNamed(Items.LIME_STAINED_GLASS_PANE, "2");
			case 3 -> literalNamed(Items.RED_STAINED_GLASS_PANE, "3");
			case 4 -> literalNamed(Items.PURPLE_STAINED_GLASS_PANE, "4");
			case 5 -> literalNamed(Items.ORANGE_STAINED_GLASS_PANE, "5");
			case 6 -> literalNamed(Items.CYAN_STAINED_GLASS_PANE, "6");
			case 7 -> literalNamed(Items.LIGHT_GRAY_STAINED_GLASS_PANE, "7");
			case 8 -> literalNamed(Items.MAGENTA_STAINED_GLASS, "8");
			default -> throw new IllegalStateException("Invalid tile: %s".formatted(tile));
		};
	}

	public static SimpleNamedScreenHandlerFactory createScreenFactory() {
		return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> {
			return new MinesweeperScreen(player, syncId);
		}, Text.literal("Minesweeper"));
	}

	private static class Grid {
		private static final Random RANDOM = new Random();
		private static final int MINE_COUNT = 12;

		private final int[] tiles;
		private final int width;
		private final int capacity;

		private long startTime = 0;

		private Grid(int width, int height) {
			this.width = width;
			this.capacity = width * height;
			this.tiles = new int[this.capacity];
		}

		public void checkGenerated(int first) {
			if (this.startTime == 0) {
				this.generate(first);
				this.startTime = System.nanoTime();
			}
		}

		public int getTile(int index) {
			return index < 0 || index >= this.capacity ? Integer.MIN_VALUE : this.tiles[index];
		}

		public int getMineCount() {
			return MINE_COUNT;
		}

		private void generate(int first) {
			int count = MINE_COUNT;
			while (count > 0) {
				int index = RANDOM.nextInt(this.capacity);
				if (index != first && !this.isMine(index)) {
					this.setMine(index);
					// Super jank way of checking but it works :P
					if (this.countMines(first) > 0) {
						this.tiles[index] = 0;
						continue;
					}
					count--;
				}
			}

			for (int i = 0; i < this.capacity; i++) {
				this.tiles[i] = this.countMines(i);
			}
		}

		private int countMines(int index) {
			// If it's a mine we don't count the mines.
			if (this.isMine(index)) {
				return -1;
			}

			int mines = 0;
			for (int surrounding : this.getSurroundingIndices(index)) {
				if (this.isMine(surrounding)) {
					mines++;
				}
			}
			return mines;
		}

		private IntSet getSurroundingIndices(int index) {
			// Eliminates the indexes we need to check.
			boolean isLeft = index % this.width == 0;
			boolean isRight = (index + 1) % (this.width) == 0;
			boolean isTop = (index - this.width) < 0;
			boolean isBottom = (index + this.width) > this.capacity;

			IntSet surrounding = new IntArraySet();
			// Could probably improve this but eh.
			if (!isLeft) {
				surrounding.add(index - 1);
				if (!isTop) {
					surrounding.add(index - this.width - 1);
				}
				if (!isBottom) {
					surrounding.add(index + this.width - 1);
				}
			}
			if (!isRight) {
				surrounding.add(index + 1);
				if (!isTop) {
					surrounding.add(index - this.width + 1);
				}
				if (!isBottom) {
					surrounding.add(index + this.width + 1);
				}
			}
			if (!isTop) {
				surrounding.add(index - this.width);
			}
			if (!isBottom) {
				surrounding.add(index + this.width);
			}
			return surrounding;
		}

		private boolean isMine(int index) {
			return index >= 0 && index < this.capacity && this.tiles[index] == -1;
		}

		private void setMine(int index) {
			this.tiles[index] = -1;
		}
	}
}
