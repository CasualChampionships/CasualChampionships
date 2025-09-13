package net.casual.championships.duel.arena

import com.mojang.serialization.Codec
import com.mojang.serialization.Keyable
import net.minecraft.util.StringRepresentable

enum class DuelArenaSize: StringRepresentable {
    Small, Medium, Large;

    override fun getSerializedName(): String {
        return this.name.lowercase()
    }

    companion object {
        val CODEC: Codec<DuelArenaSize> = StringRepresentable.fromEnum(DuelArenaSize::values)
        val KEYS: Keyable = StringRepresentable.keys(entries.toTypedArray())
    }
}