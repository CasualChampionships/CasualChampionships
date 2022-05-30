package net.casualuhc.uhcmod.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Random;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block {
	private static final Random RANDOM = new Random();

	public LeavesBlockMixin(Settings settings) {
		super(settings);
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		List<ItemStack> itemStacks = super.getDroppedStacks(state, builder);

		// I know you can do this with json stuff, but I cannot be asked
		if ((this == Blocks.OAK_LEAVES || this == Blocks.DARK_OAK_LEAVES) && RANDOM.nextInt(69) == 0) {
			itemStacks.add(Items.APPLE.getDefaultStack());
		}

		return itemStacks;
	}
}
