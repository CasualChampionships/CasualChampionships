package net.casual.championships.common.util

import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.ComponentUtils.unitalicise
import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.StringTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PlayerHeadItem
import net.minecraft.world.item.component.ResolvableProfile
import java.util.*

object HeadUtils {
    fun createConsumableGoldenHead(): ItemStack {
        return ItemStack(CommonItems.GOLDEN_HEAD).named(CommonComponents.GOLDEN_HEAD.gold())
    }

    fun createConsumablePlayerHead(player: ServerPlayer): ItemStack {
        val stack = ItemStack(CommonItems.PLAYER_HEAD)
        stack.set(DataComponents.PROFILE, ResolvableProfile(player.gameProfile))
        return stack
    }

    const val GOLDEN = "ewogICJ0aW1lc3RhbXAiIDogMTY3MDg2MDkyNTE4MywKICAicHJvZmlsZUlkIiA6ICI1N2E4NzA0ZGIzZjQ0YzhmYmVhMDY0Njc1MDExZmU3YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJQaGFudG9tVHVwYWMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjk4Nzg4NTM2NTRjM2JiMjZmZDMzZTgwZjhlZDNjZGYwM2FiMzI0N2Y3MzY3ODQ2NGUwNmRhMTQzZGJkMGMxNyIKICAgIH0sCiAgICAiQ0FQRSIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjM0MGMwZTAzZGQyNGExMWIxNWE4YjMzYzJhN2U5ZTMyYWJiMjA1MWIyNDgxZDBiYTdkZWZkNjM1Y2E3YTkzMyIKICAgIH0KICB9Cn0"
}
