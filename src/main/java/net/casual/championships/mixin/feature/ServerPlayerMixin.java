package net.casual.championships.mixin.feature;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;

@Debug(export = true)
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
}
