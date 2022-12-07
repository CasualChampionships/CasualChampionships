package net.casualuhc.uhcmod.mixin;

import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SkullItem;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Objects;

@Mixin(SkullItem.class)
public class SkullItemMixin extends VerticallyAttachableBlockItem {
	public SkullItemMixin(Block standingBlock, Block wallBlock, Settings settings, Direction verticalAttachmentDirection) {
		super(standingBlock, wallBlock, settings, verticalAttachmentDirection);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		if (player != null) {
			this.giveEffects(player, context.getStack(), context.getHand());
			return ActionResult.CONSUME;
		}
		return ActionResult.PASS;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		this.giveEffects(player, stack, hand);
		return TypedActionResult.consume(player.getStackInHand(hand));
	}

	@Unique
	private void giveEffects(PlayerEntity player, ItemStack stack, Hand hand) {
		NbtCompound compound = stack.getNbt();
		NbtCompound skullOwner; boolean isGolden = false;
		if (compound != null && (skullOwner = compound.getCompound(SkullItem.SKULL_OWNER_KEY)) != null) {
			String playerName = skullOwner.getString("Name");
			if (Objects.equals(playerName, "PhantomTupac")) {
				isGolden = true;
			}
		}

		this.addStatusEffect(player, StatusEffects.REGENERATION, isGolden ? 50 : 60, isGolden ? 3 : 2);
		this.addStatusEffect(player, StatusEffects.SPEED, (isGolden ? 20 : 15) * 20, 1);
		this.addStatusEffect(player, StatusEffects.SATURATION, 5, 4);

		if (isGolden) {
			this.addStatusEffect(player, StatusEffects.ABSORPTION, 120 * 20, 0);
			this.addStatusEffect(player, StatusEffects.RESISTANCE, 5 * 20, 0);
		}

		player.swingHand(hand, true);
		stack.decrement(1);
		player.getItemCooldownManager().set(stack.getItem(), 20);
	}

	@Unique
	private void addStatusEffect(PlayerEntity player, StatusEffect effect, int duration, int amplifier) {
		player.addStatusEffect(new StatusEffectInstance(effect, duration, amplifier));
	}
}
