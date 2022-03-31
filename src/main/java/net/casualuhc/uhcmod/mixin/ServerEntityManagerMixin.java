package net.casualuhc.uhcmod.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin {

    /**
     * These mobs will have no AI
     */
    private static final Set<EntityType<?>> DISABLED_ENTITIES = Set.of(
        EntityType.PIG,
        EntityType.COW,
        EntityType.SQUID,
        EntityType.BAT,
        EntityType.GLOW_SQUID,
        EntityType.FOX,
        EntityType.COD,
        EntityType.SALMON,
        EntityType.PARROT,
        EntityType.MOOSHROOM,
        EntityType.HORSE,
        EntityType.SKELETON_HORSE,
        EntityType.CAT,
        EntityType.MULE,
        EntityType.DONKEY,
        EntityType.CHICKEN,
        EntityType.SHEEP,
        EntityType.GOAT
    );

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At("HEAD"))
    private void onAddEntity(EntityLike entity, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof MobEntity mobEntity && DISABLED_ENTITIES.contains(mobEntity.getType())) {
            mobEntity.setAiDisabled(true);
        }
    }
}
