package net.casualuhc.uhcmod.mixin;

import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.LinkedHashSet;

@Mixin(Team.class)
public class TeamMixin {
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;"))
	private <E> HashSet<E> onNewHashSet() {
		// So I can iterate players in a consistent order
		return new LinkedHashSet<>();
	}
}
