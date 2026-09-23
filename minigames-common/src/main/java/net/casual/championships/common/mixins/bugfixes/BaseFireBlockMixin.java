package net.casual.championships.common.mixins.bugfixes;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockMixin {
    @Inject(
        method = "onPlace",
        at = @At("TAIL")
    )
    private void onPlaceFire(
        BlockState state,
        Level level,
        BlockPos pos,
        BlockState oldState,
        boolean movedByPiston,
        CallbackInfo ci
    ) {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        for (Mob mob : level.getEntitiesOfClass(Mob.class, new AABB(pos))) {
            if (mob.isNoAi()) {
                EntityAccessor accessor = (EntityAccessor) mob;
                state.entityInside(level, pos, mob, accessor.getInsideEffectCollector(), true);
            }
        }
    }
}
