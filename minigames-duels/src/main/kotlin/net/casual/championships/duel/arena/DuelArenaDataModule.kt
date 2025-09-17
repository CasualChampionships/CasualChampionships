package net.casual.championships.duel.arena

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.championships.common.util.casual
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer

data class DuelArenaDataModule(
    val spawn: BlockPos,
    val teleporter: EntityTeleporter,
    val packs: List<String>
): MinigameDataModule {
    companion object: MinigameDataModule.Provider {
        private const val DUEL_ARENA_DATA = "casual_duel_arena_data.json"

        private val CODEC = RecordCodecBuilder.create { instance ->
            instance.group(
                BlockPos.CODEC.fieldOf("spawn").forGetter(DuelArenaDataModule::spawn),
                EntityTeleporter.CODEC.fieldOf("teleporter").forGetter(DuelArenaDataModule::teleporter),
                Codec.STRING.listOf().optionalFieldOf("additional_packs", listOf()).forGetter(DuelArenaDataModule::packs)
            ).apply(instance, ::DuelArenaDataModule)
        }

        override val id: ResourceLocation = casual("duel_arena_data")

        override fun get(archive: ReadableArchive, server: MinecraftServer): DuelArenaDataModule {
            return archive.parseJson(DUEL_ARENA_DATA, CODEC).getOrThrow()
        }
    }
}