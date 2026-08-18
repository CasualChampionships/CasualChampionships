package net.casual.championships.common.mixins.event;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import kotlin.collections.CollectionsKt;
import net.casual.arcade.events.GlobalEventHandler;
import net.casual.championships.common.event.PlayerLootVaultEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;
import java.util.UUID;

@Mixin(VaultState.class)
public class VaultStateMixin {
    @WrapWithCondition(
        method = "tickAndGetNext",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/vault/VaultState;ejectResultItem(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;F)V"
        )
    )
    private boolean broadcastLootVaultEvent(
        ServerLevel serverLevel,
        BlockPos pos,
        ItemStack itemToEject,
        float ejectionSoundProgress,
        @Local(argsOnly = true) VaultServerData serverData
    ) {
        Set<UUID> rewarded = ((VaultServerDataInvoker) serverData).invokeGetRewardedPlayers();
        UUID recent = CollectionsKt.lastOrNull(rewarded);
        if (recent != null && serverLevel.getPlayerByUUID(recent) instanceof ServerPlayer player) {
            PlayerLootVaultEvent event = new PlayerLootVaultEvent(player, itemToEject);
            GlobalEventHandler.Server.broadcast(event);
        }
        return true;
    }
}
