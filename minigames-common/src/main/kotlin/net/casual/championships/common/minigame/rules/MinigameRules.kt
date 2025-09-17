package net.casual.championships.common.minigame.rules

class MinigameRules(private val rules: List<MinigameRule>): Iterable<MinigameRule> {
    override fun iterator(): Iterator<MinigameRule> {
        return this.rules.iterator()
    }

    class Builder {
        private val rules = ArrayList<MinigameRule>()

        fun rule(block: MinigameRule.Builder.() -> Unit): Builder {
            val builder = MinigameRule.Builder()
            builder.block()
            this.rules.add(builder.build())
            return this
        }

        fun build(): MinigameRules {
            return MinigameRules(this.rules)
        }
    }

    companion object {
        fun build(block: Builder.() -> Unit): MinigameRules {
            val builder = Builder()
            builder.block()
            return builder.build()
        }
    }
}