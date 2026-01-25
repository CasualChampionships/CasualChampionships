package net.casual.championships.duel.kit

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.data.MinigameDataModule
import net.casual.arcade.utils.file.ReadableArchive
import net.casual.arcade.utils.file.ReadableArchive.Companion.parseJson
import net.casual.championships.common.util.casual
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootTable

class DuelKitsDataModule(
    private val kits: Map<String, Kit>
): MinigameDataModule {
    fun all(): Collection<Kit> {
        return this.kits.values
    }

    fun names(): Set<String> {
        return this.kits.keys
    }

    class Kit(
        val name: String,
        val display: ItemStack,
        val lootTable: LootTable
    ) {
        companion object {
            val CODEC: Codec<Kit> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.fieldOf("name").forGetter(Kit::name),
                    ItemStack.SINGLE_ITEM_CODEC.fieldOf("display").forGetter(Kit::display),
                    LootTable.DIRECT_CODEC.fieldOf("loot_table").forGetter(Kit::lootTable)
                ).apply(instance, ::Kit)
            }
        }
    }

    private data class KitsData(val kitNames: List<String>) {
        companion object {
            val CODEC: Codec<KitsData> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.STRING.listOf().fieldOf("kit_names").forGetter(KitsData::kitNames),
                ).apply(instance, ::KitsData)
            }
        }
    }

    companion object: MinigameDataModule.Provider {
        private const val DUEL_KITS_DATA = "casual_duel_kits_data.json"

        override val id: Identifier = casual("duel_kits_data")

        override fun get(archive: ReadableArchive, server: MinecraftServer): DuelKitsDataModule {
            val kitNames = archive.parseJson(DUEL_KITS_DATA, KitsData.CODEC).getOrThrow().kitNames
            val kits = linkedMapOf<String, Kit>()
            for (kitName in kitNames) {
                val kit = archive.parseJson("$kitName.json", Kit.CODEC).getOrThrow()
                kits[kit.name] = kit
            }
            return DuelKitsDataModule(kits)
        }
    }
}