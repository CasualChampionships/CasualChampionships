package net.casualuhc.uhcmod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class KitCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(
			CommandManager.literal("kit").requires(s -> GameSettings.TESTING.getValue() || s.hasPermissionLevel(4)).executes(c -> {
				ServerPlayerEntity entity = c.getSource().getPlayerOrThrow();
				entity.getInventory().clear();
				entity.getInventory().armor.set(0, Items.IRON_BOOTS.getDefaultStack());
				entity.getInventory().armor.set(1, Items.IRON_LEGGINGS.getDefaultStack());
				entity.getInventory().armor.set(2, Items.IRON_CHESTPLATE.getDefaultStack());
				entity.getInventory().armor.set(3, Items.IRON_HELMET.getDefaultStack());
				entity.getInventory().offHand.set(0, Items.SHIELD.getDefaultStack());
				entity.giveItemStack(Items.DIAMOND_AXE.getDefaultStack());
				entity.giveItemStack(Items.DIAMOND_PICKAXE.getDefaultStack());
				entity.giveItemStack(Items.BOW.getDefaultStack());
				entity.giveItemStack(count(Items.ARROW.getDefaultStack(), 32));
				entity.giveItemStack(count(Items.OAK_PLANKS.getDefaultStack(), 64));
				entity.giveItemStack(Items.WATER_BUCKET.getDefaultStack());
				entity.giveItemStack(count(PlayerUtils.generateGoldenHead(), 2));
				entity.giveItemStack(count(Items.GOLDEN_APPLE.getDefaultStack(), 3));
				entity.giveItemStack(count(Items.COOKED_PORKCHOP.getDefaultStack(), 32));
				return 1;
			})
		);
	}

	private static ItemStack count(ItemStack stack, int count) {
		stack.setCount(count);
		return stack;
	}
}
