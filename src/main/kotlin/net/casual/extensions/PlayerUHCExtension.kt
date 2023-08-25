package net.casual.extensions

import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.casual.CasualMod
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.border.WorldBorder
import net.minecraft.world.scores.Team

class PlayerUHCExtension(
    private val owner: ServerPlayer
): DataExtension {
    val border = WorldBorder()

    var originalTeam: Team? = null
    var halfHealthTicks = 0

    override fun getName(): String {
        return "UHC_PlayerUHC"
    }

    override fun deserialize(element: Tag) {
        element as CompoundTag
        val teamName = element.getString("originalTeam")
        if (teamName.isNotEmpty()) {
            this.originalTeam = this.owner.server.scoreboard.getPlayerTeam(teamName)
            if (this.originalTeam === null) {
                CasualMod.logger.warn("Failed to retrieve original team for ${this.owner.scoreboardName}")
            }
        }
        this.halfHealthTicks = element.getInt("halfHealthTicks")
    }

    override fun serialize(): Tag {
        val tag = CompoundTag()
        val team = this.originalTeam
        if (team !== null) {
            tag.putString("originalTeam", team.name)
        }
        tag.putInt("halfHealthTicks", this.halfHealthTicks)
        return tag
    }

    companion object {
        val ServerPlayer.uhc
            get() = this.getExtension(PlayerUHCExtension::class.java)
    }
}