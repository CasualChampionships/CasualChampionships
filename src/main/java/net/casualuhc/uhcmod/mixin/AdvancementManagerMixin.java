package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(AdvancementManager.class)
public class AdvancementManagerMixin {
	@Shadow
	@Final
	private Map<Identifier, Advancement> advancements;

	@Shadow
	@Final
	private Set<Advancement> roots;

	@Shadow
	@Nullable
	private AdvancementManager.Listener listener;

	@Shadow
	@Final
	private Set<Advancement> dependents;

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", remap = false))
	private void onLoadAdvancements(Map<Identifier, Advancement.Builder> advancements, CallbackInfo ci) {
		UHCAdvancements.forEachAdvancement(advancement -> {
			if (advancement.getParent() == null || this.advancements.containsKey(advancement.getParent().getId())) {
				this.advancements.put(advancement.getId(), advancement);
				if (advancement.getParent() == null) {
					this.roots.add(advancement);
					if (this.listener != null) {
						this.listener.onRootAdded(advancement);
					}
				} else {
					this.dependents.add(advancement);
					if (this.listener != null) {
						this.listener.onDependentAdded(advancement);
					}
				}
			}
		});
	}
}
