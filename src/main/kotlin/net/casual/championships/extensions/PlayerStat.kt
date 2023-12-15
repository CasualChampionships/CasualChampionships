package net.casual.championships.extensions

enum class PlayerStat(
    val merger: (old: Double, new: Double) -> Double = Double::plus,
    val defaultValue: Double = 0.0,
) {
    MinesweeperRecord({ a, b -> if (a.isNaN()) b else a.coerceAtMost(b) }, Double.NaN),
    DamageDealt,
    DamageTaken,
    Kills,
    Deaths,
    Relogs;

    fun id(): String {
        // Snake case
        val sb = StringBuilder()
        for (i in this.name.indices) {
            val c = this.name[i]
            if (c.isUpperCase()) {
                if (i != 0) {
                    sb.append('_')
                }
                sb.append(c.lowercaseChar())
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }
}