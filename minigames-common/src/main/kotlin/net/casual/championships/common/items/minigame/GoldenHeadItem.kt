package net.casual.championships.common.items.minigame

import com.google.common.collect.ImmutableMultimap
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import net.casual.arcade.utils.component.gold
import net.casual.arcade.utils.player.StaticResolvableProfile
import net.casual.championships.common.util.CasualComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects.*
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ResolvableProfile

class GoldenHeadItem(properties: Properties): HeadItem(properties) {
    override fun getResolvableProfile(stack: ItemStack): ResolvableProfile {
        return this.createProfileComponent(GOLDEN)
    }

    override fun addEffects(player: ServerPlayer) {
        player.addEffect(MobEffectInstance(REGENERATION, 50, 3))
        player.addEffect(MobEffectInstance(SPEED, 20 * 20, 1))
        player.addEffect(MobEffectInstance(SATURATION, 5, 4))

        player.addEffect(MobEffectInstance(ABSORPTION, 120 * 20, 0))
        player.addEffect(MobEffectInstance(RESISTANCE, 5 * 20, 0))
    }

    override fun getName(stack: ItemStack): Component {
        return CasualComponents.GOLDEN_HEAD.gold()
    }

    @Suppress("SameParameterValue")
    private fun createProfileComponent(value: String): ResolvableProfile {
        val properties = PropertyMap(ImmutableMultimap.of("textures", Property("textures", value, null)))
        return StaticResolvableProfile(properties = properties)
    }

    private companion object {
        const val GOLDEN = "ewogICJ0aW1lc3RhbXAiIDogMTY3MDg2MDkyNTE4MywKICAicHJvZmlsZUlkIiA6ICI1N2E4NzA0ZGIzZjQ0YzhmYmVhMDY0Njc1MDExZmU3YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQaGFudG9tVHVwYWMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjk4Nzg4NTM2NTRjM2JiMjZmZDMzZTgwZjhlZDNjZGYwM2FiMzI0N2Y3MzY3ODQ2NGUwNmRhMTQzZGJkMGMxNyIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0MGMwZTAzZGQyNGExMWIxNWE4YjMzYzJhN2U5ZTMyYWJiMjA1MWIyNDgxZDBiYTdkZWZkNjM1Y2E3YTkzMyIKICAgIH0KICB9Cn0"
        const val GOLDEN_PRESENT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE3MmQwYTBkNjk2OTIxNmI3ZjBiMmY5OWFkYjQwOTk0NWM1ZGU5YjA4MzFmZjVlZjA2NGJhNWYzODM1ZTY5NiJ9fX0="
    }
}