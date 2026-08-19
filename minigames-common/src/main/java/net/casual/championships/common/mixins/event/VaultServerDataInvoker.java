package net.casual.championships.common.mixins.event;

import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public interface VaultServerDataInvoker {
    @Invoker("getRewardedPlayers")
    Set<UUID> invokeGetRewardedPlayers();
}
