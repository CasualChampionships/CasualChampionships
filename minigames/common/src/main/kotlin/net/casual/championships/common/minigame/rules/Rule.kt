package net.casual.championships.common.minigame.rules

import net.minecraft.network.chat.Component

data class Rule(
    val title: Component,
    val entries: List<RuleEntry>
) {
    class Builder {
        private val entries = ArrayList<RuleEntry>()

        var title: Component = Component.empty()

        fun entry(block: RuleEntry.Builder.() -> Unit): Builder {
            val builder = RuleEntry.Builder()
            builder.block()
            this.entries.add(builder.build())
            return this
        }

        fun build(): Rule {
            return Rule(this.title, this.entries)
        }
    }
}