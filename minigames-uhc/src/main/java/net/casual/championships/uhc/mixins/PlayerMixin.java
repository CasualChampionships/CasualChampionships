package net.casual.championships.uhc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @WrapOperation(
        method = "causeFoodExhaustion",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V"
        )
    )
    private void tryExhaustShared(
        FoodData instance,
        float exhaustion,
        Operation<Void> original
    ) {
        TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension((Player) (Object) this);
        if (extension != null) {
            extension.addExhaustion(exhaustion);
        } else {
            original.call(instance, exhaustion);
        }
    }

    @ModifyExpressionValue(
        method = "canEat",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;needsFood()Z"
        )
    )
    private boolean replaceWithSharedNeedsFood(boolean original) {
        TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension((Player) (Object) this);
        if (extension != null) {
            return extension.needsFood();
        }
        return original;
    }

    @ModifyExpressionValue(
        method = "hasEnoughFoodToDoExhaustiveManoeuvres",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;hasEnoughFood()Z"
        )
    )
    private boolean replaceWithSharedEnoughFood(boolean original) {
        TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension((Player) (Object) this);
        if (extension != null) {
            return extension.hasEnoughFood();
        }
        return original;
    }
}
