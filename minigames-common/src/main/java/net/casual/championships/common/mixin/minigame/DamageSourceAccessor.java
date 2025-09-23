package net.casual.championships.common.mixin.minigame;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DamageSource.class)
public interface DamageSourceAccessor {
    @Mutable
    @Accessor("causingEntity")
    void setCausingEntity(Entity entity);

    @Mutable
    @Accessor("directEntity")
    void setDirectEntity(Entity entity);
}
