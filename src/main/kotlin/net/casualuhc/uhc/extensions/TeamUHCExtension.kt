package net.casualuhc.uhc.extensions

import net.casualuhc.arcade.extensions.DataExtension
import net.casualuhc.arcade.utils.TeamUtils.getExtension
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.world.scores.Team

class TeamUHCExtension: DataExtension {
    val players = ArrayList<String>()

    fun add(player: String) {
        if (!this.players.contains(player)) {
            this.players.add(player)
        }
    }

    override fun getName(): String {
        return "UHC_TeamUHC"
    }

    override fun deserialize(element: Tag) {
        for (tag in element as ListTag) {
            val name = (tag as StringTag).asString
            this.add(name)
        }
    }

    override fun serialize(): Tag {
        val players = ListTag()
        for (player in this.players) {
            players.add(StringTag.valueOf(player))
        }
        return players
    }

    companion object {
        val Team.uhc
            get() = this.getExtension(TeamUHCExtension::class.java)
    }
}