package net.casual.championships.uhc.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.effect.SaturationMobEffect")
public class SaturationMobEffectMixin {
    @WrapOperation(
        method = "applyEffectTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"
        )
    )
    private void tryEatSharedHunger(
        FoodData instance,
        int foodLevelModifier,
        float saturationLevelModifier,
        Operation<Void> original,
        @Local Player player
    ) {
        TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension(player);
        if (extension != null) {
            extension.eat(foodLevelModifier, saturationLevelModifier);
        } else {
            original.call(instance, foodLevelModifier, saturationLevelModifier);
        }
    }
}
