package net.casual.championships.uhc.advancement

import com.mojang.serialization.Codec
import net.casual.arcade.utils.string.PascalCase
import net.casual.arcade.utils.string.SnakeCase
import net.casual.arcade.utils.string.convertCasing
import net.minecraft.util.StringRepresentable

enum class UHCRaceAdvancement: StringRepresentable {
    Craft, Death, Kill;

    override fun getSerializedName(): String {
        return this.name.convertCasing(PascalCase, SnakeCase)
    }

    companion object {
        val CODEC: Codec<UHCRaceAdvancement> = StringRepresentable.fromEnum(::values)
    }
}