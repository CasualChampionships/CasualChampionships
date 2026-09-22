package net.casual.championships.uhc.minigame

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.template.level.VanillaLikeLevelsTemplate
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.serialization.codec.setOf
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import java.util.*

class UHCMinigameFactory(
    private val dimensions: VanillaLikeLevelsTemplate,
    private val nerfedPlayers: Set<UUID>
): MinigameFactory {
    override fun codec(): MapCodec<out MinigameFactory> {
        return codec
    }

    override fun create(context: MinigameCreationContext): UHCMinigame {
        return UHCMinigame(context.server, context.uuid, this.nerfedPlayers, this.dimensions)
    }

    companion object: CodecProvider<UHCMinigameFactory> {
        override val id: Identifier
            get() = UHCMinigame.ID

        override val codec: MapCodec<out UHCMinigameFactory> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                VanillaLikeLevelsTemplate.CODEC.optionalFieldOf("dimensions", VanillaLikeLevelsTemplate()).forGetter(UHCMinigameFactory::dimensions),
                UUIDUtil.STRING_CODEC.setOf().lenientOptionalFieldOf("nerfed_players", emptySet()).forGetter(UHCMinigameFactory::nerfedPlayers)
            ).apply(instance, ::UHCMinigameFactory)
        }

        val DEFAULT = UHCMinigameFactory(VanillaLikeLevelsTemplate(), setOf())
    }
}
