package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Inject(method = "interactBlock",
        slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", ordinal = 0)),
        at = @At(value = "FIELD", target = "Lnet/minecraft/advancement/criterion/Criteria;ITEM_USED_ON_BLOCK:Lnet/minecraft/advancement/criterion/ItemCriterion;", ordinal = 0),
        locals = LocalCapture.CAPTURE_FAILHARD)
    private void onBlockPlaced(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir, BlockPos pos, BlockState oldState, boolean bl, boolean bl2, ItemStack stack1, ItemUsageContext context) {
        if (PlayerUtils.detectFlexibleBlockPlacement(world, hitResult.getBlockPos(), hitResult.getSide(), oldState, context)) {
            PlayerUtils.grantAdvancement(player, UHCAdvancements.BUSTED);
        }
    }
}
