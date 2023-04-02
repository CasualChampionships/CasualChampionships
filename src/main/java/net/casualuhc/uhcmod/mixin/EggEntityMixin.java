package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.uhcs.EasterUHC;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EggEntity.class)
public abstract class EggEntityMixin extends ThrownItemEntity {
	public EggEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
		super(entityType, world);
	}

	@Redirect(
		method = "onCollision",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/EntityType;create(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"
		)
	)
	private <T extends Entity> T onCreateChicken(EntityType<T> instance, World world) {
		if (Config.CURRENT_UHC instanceof EasterUHC) {
			RabbitEntity rabbit = EntityType.RABBIT.create(this.world);
			if (rabbit != null) {
				rabbit.setBreedingAge(-24000);
				rabbit.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0F);
				this.world.spawnEntity(rabbit);
			}
			return null;
		}
		return instance.create(world);
	}
}
