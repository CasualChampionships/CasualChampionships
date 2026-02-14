package net.casual.championships.uhc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @WrapWithCondition(
        method = "doTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;tick(Lnet/minecraft/server/level/ServerPlayer;)V"
        )
    )
    private boolean shouldTickNonSharedHunger(FoodData instance, ServerPlayer player) {
        return TeamSharedHealthExtension.getSharedHealthExtension(player) == null;
    }

    @ModifyExpressionValue(
        method = "doTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;getFoodLevel()I"
        )
    )
    private int getSharedFoodLevel(int original) {
        TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension((ServerPlayer) (Object) this);
        if (extension != null) {
            return extension.getFoodLevel();
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "doTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;getSaturationLevel()F"
        )
    )
    private float getSharedSaturation(float original) {
        TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension((ServerPlayer) (Object) this);
        if (extension != null) {
            return extension.getSaturationLevel();
        }
        return original;
    }
}
