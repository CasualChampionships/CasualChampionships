package net.casualuhc.uhcmod.mixin;
;
import net.casualuhc.uhcmod.interfaces.IntRuleMixinInterface;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GameRules.IntRule.class)
public abstract class IntRuleMixin extends GameRules.Rule<GameRules.IntRule> implements IntRuleMixinInterface {

	public IntRuleMixin(GameRules.Type<GameRules.IntRule> type) {
		super(type);
	}

	@Shadow
	private int value;

	@Override
	public void setIntegerValue(int newValue, MinecraftServer server) {
		this.value = newValue;
		this.changed(server);
	}
}
