package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.uhc.OneTimeAchievement;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract Item getItem();

	@Inject(method = "onCraft", at = @At("HEAD"))
	private void onCraft(World world, PlayerEntity player, int amount, CallbackInfo ci) {
		if (player instanceof ServerPlayerEntity serverPlayer && this.getItem() == Items.CRAFTING_TABLE && GameManager.isUnclaimed(OneTimeAchievement.CRAFT)) {
			PlayerManager.grantAdvancement(serverPlayer, UHCAdvancements.WORLD_RECORD_PACE);
		}
	}
}
