package net.casual.championships.common.util

import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.color
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.championships.common.minigame.rules.Rules
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

object RuleUtils {
    fun Rules.Builder.addRule(key: String, vararg entries: Int) {
        this.addRule(key, *entries.map { it to 9.Seconds }.toTypedArray())
    }

    fun Rules.Builder.addRule(key: String, vararg entries: Pair<Int, MinecraftTimeDuration>) {
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

    fun formatTitle(component: MutableComponent): MutableComponent {
        return component.withMiniFont().bold().lime()
    }

    fun formatLine(component: MutableComponent): MutableComponent {
        return component.withMiniFont().color(0x92ddfe)
    }
}