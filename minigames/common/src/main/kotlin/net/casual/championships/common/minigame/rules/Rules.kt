package net.casual.championships.common.minigame.rules

class Rules(private val rules: List<Rule>): Iterable<Rule> {
    override fun iterator(): Iterator<Rule> {
        return this.rules.iterator()
    }

    class Builder {
        private val rules = ArrayList<Rule>()

        fun rule(block: Rule.Builder.() -> Unit): Builder {
            val builder = Rule.Builder()
            builder.block()
            this.rules.add(builder.build())
            return this
        }

        fun build(): Rules {
            return Rules(this.rules)
        }
    }

    companion object {
        fun build(block: Builder.() -> Unit): Rules {
            val builder = Builder()
            builder.block()
            return builder.build()
        }
    }
}