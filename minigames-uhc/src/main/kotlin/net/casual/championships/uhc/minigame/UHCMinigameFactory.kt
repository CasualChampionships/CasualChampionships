package net.casual.championships.uhc.minigame

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevels
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevelsBuilder
import net.casual.arcade.dimensions.utils.loadCustomLevel
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.utils.setOf
import net.casual.championships.uhc.utils.UHCDimensions
import net.minecraft.core.UUIDUtil
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.WorldOptions
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class DimensionWithSeed(
    val key: Optional<DimensionWithPersistence>,
    val seed: Optional<Long>
) {
    companion object {
        val DEFAULT = DimensionWithSeed(Optional.empty(), Optional.empty())

        val CODEC: Codec<DimensionWithSeed> = RecordCodecBuilder.create { instance ->
            instance.group(
                DimensionWithPersistence.CODEC.optionalFieldOf("dimension").forGetter(DimensionWithSeed::key),
                Codec.LONG.optionalFieldOf("seed").forGetter(DimensionWithSeed::seed)
            ).apply(instance, ::DimensionWithSeed)
        }
    }

    data class DimensionWithPersistence(
        val key: ResourceKey<Level>,
        val persist: Boolean = true
    ) {
        companion object {
            private val SIMPLE_CODEC: Codec<DimensionWithPersistence> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ResourceKey.codec(Registries.DIMENSION).fieldOf("id").forGetter(DimensionWithPersistence::key),
                    Codec.BOOL.optionalFieldOf("persist", true).forGetter(DimensionWithPersistence::persist)
                ).apply(instance, ::DimensionWithPersistence)
            }

            private val ALTERNATE_CODEC = ResourceKey.codec(Registries.DIMENSION)
                .xmap(::DimensionWithPersistence, DimensionWithPersistence::key)

            val CODEC: Codec<DimensionWithPersistence> = Codec.withAlternative(SIMPLE_CODEC, ALTERNATE_CODEC)
        }
    }
}

class UHCMinigameFactory(
    private val dimensions: Map<VanillaDimension, DimensionWithSeed>,
    private val nerfedPlayers: Set<UUID>
): MinigameFactory {
    override fun codec(): MapCodec<out MinigameFactory> {
        return CODEC
    }

    override fun create(context: MinigameCreationContext): UHCMinigame {
        val dimensionsCopy = HashMap(this.dimensions)
        for (entry in VanillaDimension.entries) {
            dimensionsCopy.putIfAbsent(entry, DimensionWithSeed.DEFAULT)
        }

        val existingLevels = mutableMapOf<VanillaDimension, CustomLevel>()
        for ((vanillaDimension, dimension) in this.dimensions) {
            val persisted = dimension.key.getOrNull() ?: continue
            if (!context.server.levelKeys().contains(persisted.key)) {
                val level = CustomLevel.read(context.server, persisted.key)
                if (level != null) {
                    existingLevels[vanillaDimension] = level
                }
            }
        }

        val dimensions = if (existingLevels.size == this.dimensions.size) {
            UHCDimensions(
                UHCDimensions.LevelWithPersistence(existingLevels[VanillaDimension.Overworld]!!, true),
                UHCDimensions.LevelWithPersistence(existingLevels[VanillaDimension.Nether]!!, true),
                UHCDimensions.LevelWithPersistence(existingLevels[VanillaDimension.End]!!, true),
            )
        } else {
            val seed = WorldOptions.randomSeed()
            val levels = VanillaLikeLevelsBuilder.build(context.server) {
                for (entry in dimensionsCopy.entries) {
                    val (dimension, data) = entry
                    val key = data.key.map { it.key }
                        .orElseGet { randomDimensionKey(dimension.getDimensionKey().identifier().path) }
                    val persistence = data.key.map { it.persist }.orElse(false)

                    // We update the copy so that our factory knows what random dimension key we used
                    val updated = data.copy(
                        key = Optional.of(DimensionWithSeed.DimensionWithPersistence(key, persistence))
                    )
                    dimensionsCopy[dimension] = updated

                    set(dimension) {
                        dimensionKey(key)
                        seed(data.seed.orElse(seed))
                        defaultLevelProperties()
                        persistence(LevelPersistence.Permanent)
                    }
                }
            }

            UHCDimensions(
                this.createLevelWithPersistence(levels, VanillaDimension.Overworld, dimensionsCopy),
                this.createLevelWithPersistence(levels, VanillaDimension.Nether, dimensionsCopy),
                this.createLevelWithPersistence(levels, VanillaDimension.End, dimensionsCopy),
            )
        }
        return UHCMinigame(
            context.server,
            context.uuid,
            this.nerfedPlayers,
            dimensions,
            UHCMinigameFactory(dimensionsCopy, this.nerfedPlayers)
        )
    }

    private fun randomDimensionKey(dimension: String): ResourceKey<Level> {
        return ResourceKey.create(Registries.DIMENSION, IdentifierUtils.random { "${dimension}_$it" })
    }

    private fun createLevelWithPersistence(
        levels: VanillaLikeLevels,
        dimension: VanillaDimension,
        definitions: Map<VanillaDimension, DimensionWithSeed>
    ): UHCDimensions.LevelWithPersistence {
        val level = levels.getOrThrow(dimension)
        val persist = definitions.getOrDefault(dimension, DimensionWithSeed.DEFAULT).key
            .map { it.persist }.orElse(false)
        return UHCDimensions.LevelWithPersistence(level, persist)
    }

    companion object: CodecProvider<UHCMinigameFactory> {
        override val ID: Identifier
            get() = UHCMinigame.ID

        override val CODEC: MapCodec<out UHCMinigameFactory> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.simpleMap(
                    VanillaDimension.CODEC,
                    DimensionWithSeed.CODEC,
                    StringRepresentable.keys(VanillaDimension.entries.toTypedArray())
                ).fieldOf("dimensions").forGetter(UHCMinigameFactory::dimensions),
                UUIDUtil.STRING_CODEC.setOf().lenientOptionalFieldOf("nerfed_players", emptySet()).forGetter(UHCMinigameFactory::nerfedPlayers)
            ).apply(instance, ::UHCMinigameFactory)
        }

        val DEFAULT = UHCMinigameFactory(mapOf(), setOf())
    }
}