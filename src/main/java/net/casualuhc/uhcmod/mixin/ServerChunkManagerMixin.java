package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.UHCMod;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerChunkManager.class)
public class ServerChunkManagerMixin {

    @Unique
    boolean didNotTick = false;

    /**
     * This essentially disables spawning and random ticks if server is lagging
     */
    @ModifyVariable(method = "tickChunks", at = @At(value = "STORE", ordinal = 0))
    private boolean shouldNotTick(boolean original) {
        if (UHCMod.calculateMSPT() > 60) {
            this.didNotTick = true;
        }
        else if (this.didNotTick) {
            this.didNotTick = false;
        }
        return this.didNotTick;
    }
}
