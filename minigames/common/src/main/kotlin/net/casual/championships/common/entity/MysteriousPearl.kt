package net.casual.championships.common.entity

import eu.pb4.polymer.core.api.entity.PolymerEntity
import net.casual.championships.common.item.CommonItems
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

class MysteriousPearl: ThrowableItemProjectile, PolymerEntity {
    constructor(type: EntityType<out MysteriousPearl>, level: Level): super(type, level)

    constructor(level: Level, shooter: LivingEntity): super(CommonEntities.MYSTERIOUS_PEARL, shooter, level)

    init {
        this.item = CommonItems.MYSTERIOUS_PEARL.defaultInstance
    }

    override fun getPolymerEntityType(player: ServerPlayer?): EntityType<*> {
        return EntityType.SNOWBALL
    }

    override fun getDefaultItem(): Item {
        return Items.AIR
    }

    override fun onHit(result: HitResult) {
        super.onHit(result)

        this.level().explode(this, this.x, this.y, this.z, 3F, Level.ExplosionInteraction.NONE)
    }
}