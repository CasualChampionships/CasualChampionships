package net.casual.championships.common.mixin.event;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.common.event.TippedArrowTradeOfferEvent;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.item.alchemy.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VillagerTrades.TippedArrowForItemsAndEmeralds.class)
public class TippedArrowForItemsAndEmeraldsMixin {
	@Definition(id = "getRandom", method = "Lnet/minecraft/util/Util;getRandom(Ljava/util/List;Lnet/minecraft/util/RandomSource;)Ljava/lang/Object;")
	@Definition(id = "Holder", type = Holder.class)
	@Expression("(Holder) getRandom(?, ?)")
	@ModifyExpressionValue(
		method = "getOffer",
		at = @At(value = "MIXINEXTRAS:EXPRESSION")
	)
	private Holder<Potion> onGetRandomPotion(Holder<Potion> original, @Local(argsOnly = true) Entity trader) {
		TippedArrowTradeOfferEvent event = new TippedArrowTradeOfferEvent(trader, original);
		GlobalEventHandler.Server.broadcast(event);
		return event.getPotion();
	}
}
