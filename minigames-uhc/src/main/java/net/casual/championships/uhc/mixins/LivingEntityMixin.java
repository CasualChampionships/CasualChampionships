package net.casual.championships.uhc.mixins;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.casual.arcade.utils.PlayerUtils;
import net.casual.championships.uhc.extensions.TeamSharedHealthExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(
        method = "heal",
        at = @At("HEAD"),
        cancellable = true
    )
    private void tryHealShared(float healAmount, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player) {
            TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension(player);
            if (extension != null) {
                extension.heal(PlayerUtils.getServer(player), healAmount);
                ci.cancel();
            }
        }
    }

    @Inject(
        method = "getHealth",
        at = @At("HEAD"),
        cancellable = true
    )
    private void getSharedHealth(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof ServerPlayer player) {
            TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension(player);
            if (extension != null) {
                cir.setReturnValue(extension.getHealth());
            }
        }
    }

    @WrapOperation(
        method = "hurtServer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;checkTotemDeathProtection(Lnet/minecraft/world/damagesource/DamageSource;)Z"
        )
    )
    private boolean checkSharedTotems(
        LivingEntity instance,
        DamageSource damageSource,
        Operation<Boolean> original
    ) {
        if (original.call(instance, damageSource)) {
            return true;
        }
        if ((Object) this instanceof ServerPlayer player) {
            TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension(player);
            if (extension != null) {
                for (ServerPlayer teammate : extension.teammates(PlayerUtils.getServer(player))) {
                    if (teammate != player && original.call(teammate, damageSource)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Definition(id = "setHealth", method = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V")
    @Expression("?.setHealth(1.0)")
    @WrapOperation(
        method = "checkTotemDeathProtection",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private void tryReviveShared(
        LivingEntity instance,
        float health,
        Operation<Void> original
    ) {
        if ((Object) this instanceof ServerPlayer player) {
            TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension(player);
            if (extension != null) {
                extension.set(PlayerUtils.getServer(player), health);
                return;
            }
        }
        original.call(instance, health);
    }

    @Definition(id = "internalSetAbsorptionAmount", method = "Lnet/minecraft/world/entity/LivingEntity;internalSetAbsorptionAmount(F)V")
    @Expression("?.internalSetAbsorptionAmount(@(?))")
    @ModifyExpressionValue(
        method = "setAbsorptionAmount",
        at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private float syncSharedAbsorption(float original) {
        if ((Object) this instanceof ServerPlayer player) {
            TeamSharedHealthExtension extension = TeamSharedHealthExtension.getSharedHealthExtension(player);
            if (extension != null) {
                extension.setAbsorption(original, player);
            }
        }
        return original;
    }
}
