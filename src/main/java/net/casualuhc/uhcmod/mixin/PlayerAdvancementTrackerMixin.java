package net.casualuhc.uhcmod.mixin;

import net.casualuhc.arcade.advancements.AdvancementHandler;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.advancement.PlayerAdvancementTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {
	@Redirect(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementDisplay;shouldAnnounceToChat()Z"))
	private boolean shouldAnnounceAdvancement(AdvancementDisplay instance, Advancement advancement) {
		return AdvancementHandler.isCustom(advancement) && instance.shouldAnnounceToChat();
	}
}
