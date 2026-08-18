package net.casual.championships.common.mixins.event;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.common.event.CreateTradeOfferEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerTrade.class)
public class VillagerTradeMixin {
	@ModifyExpressionValue(
		method = "getOffer",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/item/ItemStackTemplate;create()Lnet/minecraft/world/item/ItemStack;"
		)
	)
	private ItemStack broadcastCreateTradeOfferEvent(ItemStack original, LootContext context) {
		CreateTradeOfferEvent event = new CreateTradeOfferEvent(context, original);
		GlobalEventHandler.Server.broadcast(event);
		return event.getOffer();
	}
}
