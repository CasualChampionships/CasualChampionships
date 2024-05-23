package net.casual.championships.common.minigame.rule

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.network.chat.Component

class RuleEntry(
    val lines: List<Component>,
    val time: MinecraftTimeDuration
) {
    class Builder {
        private val lines = ArrayList<Component>()

        var time: MinecraftTimeDuration = 4.Seconds

        fun line(component: Component) {
            this.lines.add(component)
        }

        fun build(): RuleEntry {
            return RuleEntry(this.lines, this.time)
        }
    }
}