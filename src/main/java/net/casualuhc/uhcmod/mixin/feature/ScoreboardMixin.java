package net.casualuhc.uhcmod.mixin.feature;

import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.LinkedHashMap;

@Mixin(Scoreboard.class)
public class ScoreboardMixin {
	@Redirect(
		method = "<init>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;",
			ordinal = 3,
			remap = false
		)
	)
	private <K, V> HashMap<K, V> onNewHashMap() {
		// So I can iterate teams in a consistent order
		return new LinkedHashMap<>();
	}
}
