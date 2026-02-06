package net.casual.championships.uhc.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {
    @WrapOperation(
        method = "onConsume",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/food/FoodProperties;)V"
        )
    )
    private void tryEatSharedHunger(
        FoodData instance,
        FoodProperties foodProperties,
        Operation<Void> original,
        @Local Player player
    ) {
        TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension(player);
        if (extension != null) {
            extension.eat(foodProperties);
        } else {
            original.call(instance, foodProperties);
        }
    }
}
