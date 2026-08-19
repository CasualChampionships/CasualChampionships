package net.casual.championships.resources

import com.google.common.collect.HashBiMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.pack.PackInfo
import net.casual.arcade.pack.generation.BuiltInResourcePacks
import net.casual.arcade.pack.generation.PackDefinition
import net.casual.arcade.pack.host.GlobalPackHost
import net.casual.arcade.pack.host.PackHost.HostedPackRef
import net.casual.arcade.pack.host.PathPack
import net.casual.arcade.pack.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.utils.scoreboard.getHexColor
import net.casual.championships.CasualChampionships
import net.casual.championships.common.CasualCommon
import net.casual.championships.common.util.CasualUtils
import net.casual.championships.uhc.CasualUHC
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.TeamColor
import java.util.*
import kotlin.io.path.listDirectoryEntries

class CasualPacks(
    private val championships: CasualChampionships
) {
    private val directory = CasualUtils.resolve("packs")
    private val generated = this.directory.resolve("generated")

    private val host = GlobalPackHost
    private val common = LinkedHashMap<String, HostedPackRef>()

    private val colors = Object2IntOpenHashMap<TeamColor>()

    val uhc = this.hostResources(CasualUHC.UHC_PACK)
    val boundary = this.hostResources(BuiltInResourcePacks.BOUNDARY_SHADER_PACK)

    init {
        for (pack in this.directory.listDirectoryEntries("*.zip")) {
            this.host.add(PathPack(pack))
        }
        for (definition in CasualCommon.COMMON_PACKS) {
            this.hostCommon(definition)
        }
    }

    fun createGlobalPacks(additional: List<String>): List<PackInfo> {
        val packs = ArrayList<PackInfo>(this.common.size + additional.size)
        this.common.values.mapTo(packs) { ref -> ref.value.toPackInfo() }
        packs.addAll(this.resolve(additional))
        return packs
    }

    fun createResources(packs: () -> List<String>): MinigameResources {
        return object: MinigameResources {
            override fun getPacks(): Collection<PackInfo> {
                return resolve(packs.invoke())
            }
        }
    }

    fun loadTeamColors(teams: Collection<PlayerTeam>): Boolean {
        var index = 0
        val colors = HashBiMap.create<TeamColor, Int>()
        for (team in teams) {
            val color = team.getHexColor()?.coerceIn(-1, 0xFFFFFF) ?: continue
            var original = colors.inverse()[color]
            if (original == null) {
                if (index >= 16) {
                    CasualUtils.logger.error("Tried to load more team colors than were available!!")
                    continue
                }
                original = TeamColor.entries[index++]
            }
            colors[original] = color
            team.color = Optional.of(original)
        }

        if (this.colors == colors) {
            return false
        }
        this.colors.clear()
        this.colors.putAll(colors)
        this.hostCommon(BuiltInResourcePacks.createCustomGlowColorPack {
            for ((formatting, color) in colors) {
                if (color != -1) {
                    set(formatting, color)
                } else {
                    rainbow(formatting)
                }
            }
        })
        return true
    }

    private fun resolve(names: List<String>): List<PackInfo> {
        return names.mapNotNull { name -> this.host.get(name)?.toPackInfo(!this.championships.config.dev) }
    }

    private fun hostResources(definition: PackDefinition): MinigameResources {
        val hosted by this.host(definition)
        return object: MinigameResources {
            override fun getPacks(): Collection<PackInfo> {
                return listOf(hosted.toPackInfo(!championships.config.dev))
            }
        }
    }

    private fun hostCommon(definition: PackDefinition) {
        this.common[definition.name] = this.host(definition)
    }

    private fun host(definition: PackDefinition): HostedPackRef {
        return this.host.add(PathPack(definition.buildTo(this.generated)))
    }
}
