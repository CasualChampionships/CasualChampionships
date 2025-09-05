package net.casual.championships.common.util

import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.championships.common.minigame.rules.MinigameRuleEntry
import net.casual.championships.common.minigame.rules.MinigameRules
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

fun MinigameRules.Builder.addRule(key: String, vararg entries: Int) {
    this.addRule(key, *entries.map { it to 9.Seconds }.toTypedArray())
}

fun MinigameRules.Builder.addRule(key: String, vararg entries: Pair<Int, MinecraftTimeDuration>) {
    if (entries.isEmpty()) {
        throw IllegalArgumentException("You must have at least one entry")
    }

    rule {
        title = formatTitle(Component.translatable(key))
        var i = 1
        for ((entry, time) in entries) {
            entry {
                duration = time
                while (i <= entry) {
                    line(formatLine(Component.translatable("${key}.${i++}")))
                }
            }
        }
    }
}

@Suppress("UnusedReceiverParameter")
fun MinigameRules.Builder.formatTitle(component: MutableComponent): MutableComponent {
    return component.withMiniFont().bold().lime()
}

@Suppress("UnusedReceiverParameter")
fun MinigameRuleEntry.Builder.formatLine(component: MutableComponent): MutableComponent {
    return RuleUtils.formatLine(component)
}

object RuleUtils {
    fun formatLine(component: MutableComponent): MutableComponent {
        return component.withMiniFont().color(0x92ddfe)
    }
}