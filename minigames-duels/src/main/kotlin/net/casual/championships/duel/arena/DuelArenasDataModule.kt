package net.casual.championships.duel.arena

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.minigame.data.module.MinigameWorldData
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.child
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.championships.common.util.casual
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStackTemplate
import java.util.*

class DuelArenasDataModule(
    private val arenas: Map<String, ResolvedArenas>
): MinigameDataModule {
    fun all(): Collection<ResolvedArenas> {
        return this.arenas.values
    }

    data class DuelArena(
        val data: DuelArenaDataModule,
        val world: MinigameWorldData
    )

    class ResolvedArenas(
        val name: String,
        val display: Component,
        val arenas: EnumMap<DuelArenaSize, DuelArena>
    )

    private class UnresolvedArenas(
        val name: String,
        val display: ItemStackTemplate,
        val arenas: Map<DuelArenaSize, String>
    ) {
        companion object {
            val CODEC: Codec<UnresolvedArenas> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("name").forGetter(UnresolvedArenas::name),
                    ItemStackTemplate.CODEC.fieldOf("display").forGetter(UnresolvedArenas::display),
                    Codec.simpleMap(DuelArenaSize.CODEC, Codec.STRING, DuelArenaSize.KEYS).forGetter(UnresolvedArenas::arenas)
                ).apply(instance, DuelArenasDataModule::UnresolvedArenas)
            }
        }
    }

    companion object: MinigameDataModule.Provider {
        private const val DUEL_ARENAS_DATA = "casual_duel_arenas_data.json"

        override val id: Identifier = casual("duel_arenas_data")

        override fun get(archive: ReadableArchive, server: MinecraftServer): DuelArenasDataModule {
            val unresolved = archive.parseJson(DUEL_ARENAS_DATA, UnresolvedArenas.CODEC.listOf()).getOrThrow()
            val resolved = LinkedHashMap<String, ResolvedArenas>()
            for (instance in unresolved) {
                val arenas = EnumUtils.mapOf<DuelArenaSize, DuelArena>()
                for ((size, name) in instance.arenas) {
                    val child = archive.child(name)
                    arenas[size] = DuelArena(
                        DuelArenaDataModule.get(child, server),
                        MinigameWorldData.get(child, server)
                    )
                }
                resolved[instance.name] = ResolvedArenas(instance.name, instance.display.create().hoverName, arenas)
            }
            return DuelArenasDataModule(resolved)
        }
    }
}