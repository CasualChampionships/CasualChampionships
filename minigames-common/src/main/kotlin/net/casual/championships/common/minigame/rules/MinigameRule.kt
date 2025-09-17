package net.casual.championships.common.minigame.rules

import net.minecraft.network.chat.Component

data class MinigameRule(
    val title: Component,
    val entries: List<MinigameRuleEntry>
) {
    class Builder {
        private val entries = ArrayList<MinigameRuleEntry>()

        var title: Component = Component.empty()

        fun entry(block: MinigameRuleEntry.Builder.() -> Unit): Builder {
            val builder = MinigameRuleEntry.Builder()
            builder.block()
            this.entries.add(builder.build())
            return this
        }

        fun build(): MinigameRule {
            return MinigameRule(this.title, this.entries)
        }
    }
}