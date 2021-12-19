package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.minecraft.scoreboard.AbstractTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractTeam.class)
public class AbstractTeamMixin implements AbstractTeamMixinInterface {

	@Unique
	private boolean ready = false;
	@Unique
	private boolean isEliminated = false;

	@Override
	public boolean isReady() {
		return this.ready;
	}

	@Override
	public void setReady(boolean isReady) {
		this.ready = isReady;
	}

	@Override
	public boolean isEliminated() {
		return isEliminated;
	}

	@Override
	public void setEliminated(boolean isEliminated) {
		this.isEliminated = isEliminated;
	}
}
