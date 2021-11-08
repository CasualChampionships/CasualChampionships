package net.casualuhc.uhcmod.mixin;
;
import net.casualuhc.uhcmod.interfaces.IntRuleMixinInterface;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameRules.IntRule.class)
public abstract class IntRuleMixin implements IntRuleMixinInterface {

	@Shadow
	private int value;

	@Override
	public void setIntegerValue(int newValue) {
		this.value = newValue;
	}
}
