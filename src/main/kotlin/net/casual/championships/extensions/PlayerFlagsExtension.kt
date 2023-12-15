package net.casual.championships.extensions

import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.*

class PlayerFlagsExtension(
    connection: ServerGamePacketListenerImpl
): PlayerExtension(connection), DataExtension {
    private val flags = EnumSet.noneOf(PlayerFlag::class.java)

    fun has(flag: PlayerFlag): Boolean {
        return this.flags.contains(flag)
    }

    fun set(flag: PlayerFlag, set: Boolean) {
        if (set) {
            if (!this.flags.add(flag)) {
                return
            }
        } else if (!this.flags.remove(flag)) {
            return
        }
        flag.trigger(this.player, set)
    }

    fun toggle(flag: PlayerFlag) {
        this.set(flag, !this.has(flag))
    }

    fun get(): Collection<PlayerFlag> {
        return this.flags
    }

    fun clear() {
        for (flag in PlayerFlag.values()) {
            this.set(flag, false)
        }
    }

    override fun getName(): String {
        return "UHC_PlayerFlags"
    }

    override fun deserialize(element: Tag) {
        (element as ListTag).forEach {
            try {
                this.flags.add(PlayerFlag.valueOf(it.asString))
            } catch (_: Exception) {

            }
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
        val ServerPlayer.flags
            get() = this.getExtension(PlayerFlagsExtension::class.java)
    }
}