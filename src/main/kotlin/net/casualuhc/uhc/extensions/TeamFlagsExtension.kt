package net.casualuhc.uhc.extensions

import net.casualuhc.arcade.extensions.DataExtension
import net.casualuhc.arcade.utils.TeamUtils.asPlayerTeam
import net.casualuhc.arcade.utils.TeamUtils.getExtension
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.world.scores.Team
import java.util.*

class TeamFlagsExtension: DataExtension {
    private val flags = EnumSet.noneOf(TeamFlag::class.java)

    fun has(flag: TeamFlag): Boolean {
        return this.flags.contains(flag)
    }

    fun set(flag: TeamFlag, set: Boolean) {
        if (set) this.flags.add(flag) else this.flags.remove(flag)
    }

    fun get(): Collection<TeamFlag> {
        return this.flags
    }

    fun toggle(flag: TeamFlag) {
        if (!this.flags.add(flag)) {
            this.flags.remove(flag)
        }
    }

    fun clear() {
        this.flags.clear()
    }

    override fun getName(): String {
        return "UHC_TeamFlags"
    }

    override fun deserialize(element: Tag) {
        (element as ListTag).forEach {
            this.flags.add(TeamFlag.valueOf(it.asString))
        }
    }

    override fun serialize(): Tag {
        val tag = ListTag()
        for (flag in this.flags) {
            tag.add(StringTag.valueOf(flag.name))
        }
        return tag
    }

    companion object {
        val Team.flags
            get() = this.asPlayerTeam().getExtension(TeamFlagsExtension::class.java)
    }
}