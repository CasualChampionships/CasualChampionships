package net.casualuhc.uhc.extensions

import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.extensions.DataExtension
import net.casualuhc.arcade.utils.PlayerUtils.getExtension
import net.casualuhc.uhc.UHCMod
import net.casualuhc.uhc.advancement.UHCAdvancements
import net.minecraft.advancements.Advancement
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.*
import java.util.stream.Stream

class PlayerStatsExtension: DataExtension {
    private val stats = EnumMap<PlayerStat, Double>(PlayerStat::class.java)
    private val advancements = HashSet<Advancement>()

    init {
        this.reset()
    }

    operator fun set(stat: PlayerStat, value: Double) {
        stats[stat] = value
    }

    operator fun get(stat: PlayerStat): Double {
        return stats[stat] ?: throw IllegalArgumentException("Tried to get non-existent player stat")
    }

    fun increment(stat: PlayerStat, value: Double) {
        this[stat] = this[stat] + value
    }

    fun add(advancement: Advancement) {
        if (UHCAdvancements.isRegistered(advancement)) {
            this.advancements.add(advancement)
        }
    }

    fun advancements(): Stream<Advancement> {
        return this.advancements.stream()
    }

    fun reset() {
        for (stat in PlayerStat.values()) {
            this.stats[stat] = stat.defaultValue
        }
        this.advancements.clear()
    }

    override fun getName(): String {
        return "UHC_PlayerStats"
    }

    override fun deserialize(element: Tag) {
        element as CompoundTag
        val advancements = element.getList("advancements", Tag.TAG_STRING.toInt())
        for (tag in advancements) {
            val string = (tag as StringTag).asString
            val id = ResourceLocation.tryParse(string)
            if (id === null) {
                UHCMod.logger.warn("Failed to load advancement $string, invalid id")
                continue
            }
            val advancement = Arcade.server.advancements.getAdvancement(id)
            if (advancement === null) {
                UHCMod.logger.warn("Failed to get advancement $id, non-existent")
                continue
            }
            this.advancements.add(advancement)
        }

        val stats = element.getCompound("stats")
        for (key in stats.allKeys) {
            val stat = PlayerStat.valueOf(key)
            val value = stats.getDouble(key)
            this.stats[stat] = value
        }
    }

    override fun serialize(): Tag {
        val tag = CompoundTag()
        val advancements = ListTag()
        for (advancement in this.advancements) {
            advancements.add(StringTag.valueOf(advancement.id.toString()))
        }
        tag.put("advancements", advancements)

        val stats = CompoundTag()
        for ((stat, value) in this.stats) {
            stats.putDouble(stat.name, value)
        }
        tag.put("stats", stats)
        return tag
    }

    companion object {
        val ServerPlayer.uhcStats
            get() = this.getExtension(PlayerStatsExtension::class.java)
    }
}