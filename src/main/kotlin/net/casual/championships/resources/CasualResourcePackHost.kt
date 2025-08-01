package net.casual.championships.resources

import com.google.common.collect.HashBiMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.casual.arcade.host.GlobalPackHost
import net.casual.arcade.host.PackHost.HostedPackRef
import net.casual.arcade.host.pack.PathPack
import net.casual.arcade.host.pack.hosted.HostedPack
import net.casual.arcade.minigame.utils.MinigameResources
import net.casual.arcade.resources.ArcadeResourcePacks
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.utils.ResourcePackUtils.addPack
import net.casual.arcade.resources.utils.ResourcePackUtils.toPackInfo
import net.casual.arcade.utils.TeamUtils.getHexColor
import net.casual.championships.CasualMod
import net.casual.championships.common.CommonMod
import net.casual.championships.common.util.CommonConfig
import net.casual.championships.uhc.UHCMod
import net.minecraft.ChatFormatting
import net.minecraft.world.scores.PlayerTeam
import kotlin.io.path.listDirectoryEntries

object CasualResourcePackHost {
    private val packs = CommonConfig.resolve("packs")
    private val generated = this.packs.resolve("generated")

    private val colors = Object2IntOpenHashMap<ChatFormatting>()

    private val host = GlobalPackHost
    private val common = HashMap<String, HostedPackRef>()

    val uhc: HostedPack by this.host(UHCMod.UHC_PACK)
    val boundary: HostedPack by this.host(ArcadeResourcePacks.BOUNDARY_SHADER_PACK)

    init {
        for (pack in this.packs.listDirectoryEntries("*.zip")) {
            this.host.add(PathPack(pack))
        }
        for (creator in CommonMod.COMMON_PACKS) {
            this.hostCommon(creator)
        }
    }

    fun getCommonPacks(): List<HostedPack> {
        return this.common.values.map(HostedPackRef::value)
    }

    fun getHostedPack(name: String): HostedPack? {
        return this.host.get(name)
    }

    fun createResourcesFromPacks(packs: () -> List<String>): MinigameResources {
        return object: MinigameResources {
            override fun getPacks(): Collection<PackInfo> {
                return packs.invoke().mapNotNull { pack ->
                    getHostedPack(pack)?.toPackInfo(!CasualMod.config.dev)
                }
            }
        }
    }

    fun loadTeamColors(teams: Collection<PlayerTeam>): Boolean {
        var index = 0
        val colors = HashBiMap.create<ChatFormatting, Int>()
        for (team in teams) {
            val color = team.getHexColor()?.coerceIn(-1, 0xFFFFFF) ?: continue
            var original = colors.inverse()[color]
            if (original == null) {
                if (index >= 16) {
                    CasualMod.logger.error("Tried to load more team colors than were available!!")
                    continue
                }
                original = ChatFormatting.entries[index++]
            }
            colors[original] = color
            team.color = original
        }

        if (this.colors != colors) {
            this.colors.clear()
            this.colors.putAll(colors)
            this.hostCommon(ArcadeResourcePacks.createCustomGlowColorPack {
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
        return false
    }

    internal fun registerEvents() {

    }

    private fun hostCommon(creator: NamedResourcePackCreator) {
        this.common[creator.zippedName()] = this.host(creator)
    }

    private fun host(creator: NamedResourcePackCreator): HostedPackRef {
        val ref = this.host.addPack(this.generated, creator)
        ref.future.thenApply(this::cachePackForReplay)
        return ref
    }

    private fun cachePackForReplay(hosted: HostedPack) {
        // try {
        //     @Suppress("DEPRECATION")
        //     val pathHash = Hashing.sha1().hashString(hosted.url.resolve(), StandardCharsets.UTF_8).toString()
        //     val path = ReplayConfig.root.resolve("packs").createDirectories().resolve(pathHash)
        //     path.outputStream().use {
        //         hosted.pack.stream().transferTo(it)
        //     }
        // } catch (e: IOException) {
        //     CasualMod.logger.error("Failed to cache pack for replays", e)
        // }
    }
}