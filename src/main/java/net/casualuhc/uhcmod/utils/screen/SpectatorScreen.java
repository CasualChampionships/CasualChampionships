package net.casualuhc.uhcmod.utils.screen;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.casualuhc.uhcmod.utils.uhc.ItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.List;

public class SpectatorScreen extends CustomScreen {
	private final boolean spectators;
	private final int page;

	public SpectatorScreen(PlayerInventory playerInventory, int syncId, int page, boolean spectators) {
		super(playerInventory, syncId, 6);
		this.page = page;
		this.spectators = spectators;

		if (this.spectators) {
			this.createSpectatorsList();
		} else {
			this.createTeamList();
		}
	}

	@Override
	public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
		if (slotIndex == 0 && this.page > 0) {
			player.openHandledScreen(SpectatorScreen.createScreenFactory(this.page - 1, this.spectators));
			return;
		}
		if (slotIndex == 7) {
			Vec3d spawn = Config.CURRENT_EVENT.getLobbySpawnPos();
			((ServerPlayerEntity) player).teleport(UHCMod.SERVER.getOverworld(), spawn.getX(), spawn.getY(), spawn.getZ(), 0, 0);
			((ServerPlayerEntity) player).interactionManager.changeGameMode(GameMode.ADVENTURE);
			return;
		}
		if (slotIndex == 8) {
			player.openHandledScreen(SpectatorScreen.createScreenFactory(this.page + 1, this.spectators));
			return;
		}
		if (slotIndex < 9 || slotIndex > 53 || (!this.spectators && slotIndex % 9 == 0)) {
			return;
		}

		ItemStack clickedStack = this.slots.get(slotIndex).getStack();
		if (clickedStack.isEmpty()) {
			return;
		}

		NbtCompound compound = clickedStack.getNbt();
		NbtCompound skullOwner;
		if (compound != null && (skullOwner = compound.getCompound(SkullItem.SKULL_OWNER_KEY)) != null) {
			String playerName = skullOwner.getString("Name");
			ServerPlayerEntity target = UHCMod.SERVER.getPlayerManager().getPlayer(playerName);
			if (target == null) {
				player.sendMessage(Text.translatable("uhc.spectator.notOnline", playerName), false);
				return;
			}
			((ServerPlayerEntity) player).teleport(target.getWorld(), target.getX(), target.getY(), target.getZ(), target.getYaw(), target.getPitch());
		}
	}

	private void createTeamList() {
		this.modifyInventory(inventory -> {
			List<Team> teamList = UHCMod.SERVER.getScoreboard().getTeams()
				.stream()
				.filter(t -> !TeamManager.shouldIgnoreTeam(t) && !t.getPlayerList().isEmpty() && t.getPlayerList().stream().anyMatch(p -> UHCMod.SERVER.getPlayerManager().getPlayer(p) != null))
				.skip(5L * this.page)
				.toList();

			for (int i = 1; i <= 5; i++) {
				if (teamList.size() <= (i - 1)) {
					break;
				}
				int startSlot = i * 9;
				Team team = teamList.get(i - 1);
				inventory.setStack(startSlot, getDisplay(team));

				startSlot += 1;
				List<ItemStack> itemStacks = team.getPlayerList()
					.stream()
					.filter(p -> UHCMod.SERVER.getPlayerManager().getPlayer(p) != null)
					.limit(7)
					.map(p -> ItemUtils.generatePlayerHead(p).setCustomName(Text.literal(p).styled(s -> s.withItalic(false))))
					.toList();
				int options = itemStacks.size();
				for (int j = 0; j < options; j++) {
					inventory.setStack(startSlot + j, itemStacks.get(j));
				}
			}

			for (int i = 1; i < 7; i++) {
				inventory.setStack(i, Items.GRAY_STAINED_GLASS.getDefaultStack().setCustomName(Text.literal("")));
			}

			inventory.setStack(0, ItemUtils.literalNamed(Items.RED_STAINED_GLASS, "Previous"));
			inventory.setStack(7, ItemUtils.literalNamed(Items.BLUE_STAINED_GLASS, "Return to Bleachers"));
			inventory.setStack(8, ItemUtils.literalNamed(Items.GREEN_STAINED_GLASS, "Next"));
		});
	}

	private void createSpectatorsList() {
		this.modifyInventory(inventory -> {
			List<ItemStack> itemStacks = UHCMod.SERVER.getScoreboard().getTeams()
				.stream()
				.filter(TeamManager::shouldIgnoreTeam)
				.flatMap(t -> t.getPlayerList().stream())
				.filter(p -> UHCMod.SERVER.getPlayerManager().getPlayer(p) != null)
				.skip(45L * this.page)
				.limit(45)
				.map(p -> ItemUtils.generatePlayerHead(p).setCustomName(Text.literal(p).styled(s -> s.withItalic(false))))
				.toList();
			for (int i = 0; i < itemStacks.size(); i++) {
				ItemStack stack = itemStacks.get(i);
				inventory.setStack(i + 9, stack);
			}

			for (int i = 1; i < 7; i++) {
				inventory.setStack(i, Items.GRAY_STAINED_GLASS.getDefaultStack().setCustomName(Text.literal("")));
			}

			inventory.setStack(0, ItemUtils.literalNamed(Items.RED_STAINED_GLASS, "Previous"));
			inventory.setStack(7, ItemUtils.literalNamed(Items.BLUE_STAINED_GLASS, "Teams"));
			inventory.setStack(8, ItemUtils.literalNamed(Items.GREEN_STAINED_GLASS, "Next"));
		});
	}

	private static ItemStack getDisplay(Team team) {
		String texture = switch (team.getColor()) {
			case BLACK -> ItemUtils.BLACK;
			case DARK_BLUE -> ItemUtils.DARK_BLUE;
			case DARK_GREEN -> ItemUtils.DARK_GREEN;
			case DARK_AQUA -> ItemUtils.DARK_AQUA;
			case DARK_RED -> ItemUtils.DARK_RED;
			case DARK_PURPLE -> ItemUtils.DARK_PURPLE;
			case GOLD -> ItemUtils.GOLD;
			case GRAY -> ItemUtils.GRAY;
			case DARK_GRAY -> ItemUtils.DARK_GRAY;
			case BLUE -> ItemUtils.BLUE;
			case GREEN -> ItemUtils.GREEN;
			case AQUA -> ItemUtils.AQUA;
			case RED -> ItemUtils.RED;
			case LIGHT_PURPLE -> ItemUtils.LIGHT_PURPLE;
			case YELLOW -> ItemUtils.YELLOW;
			default -> ItemUtils.WHITE;
		};
		return ItemUtils.generatePlayerHead("Dummy", texture).setCustomName(Text.literal(team.getName()).styled(s -> s.withItalic(false)));
	}

	public static SimpleNamedScreenHandlerFactory createScreenFactory(int page, boolean spectators) {
		if (page < 0) {
			return null;
		}
		return new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> {
			return new SpectatorScreen(inv, syncId, page, spectators);
		}, Text.translatable("uhc.spectator.screen"));
	}
}
