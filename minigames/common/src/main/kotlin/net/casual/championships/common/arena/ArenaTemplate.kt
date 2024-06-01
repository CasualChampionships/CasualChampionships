package net.casual.championships.common.arena

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.utils.location.teleporter.EntityTeleporter
import net.minecraft.server.level.ServerLevel

class ArenaTemplate(
    val area: PlaceableAreaTemplate,
    val teleporter: EntityTeleporter
) {
    fun create(level: ServerLevel): Arena {
        return Arena(this.area.create(level), this.teleporter)
    }

    companion object {
        val DEFAULT = ArenaTemplate(PlaceableAreaTemplate.DEFAULT, EntityTeleporter.DEFAULT)

        val CODEC: Codec<ArenaTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                PlaceableAreaTemplate.CODEC.fieldOf("area").forGetter(ArenaTemplate::area),
                EntityTeleporter.CODEC.fieldOf("teleporter").forGetter(ArenaTemplate::teleporter)
            ).apply(instance, ::ArenaTemplate)
        }
    }
}