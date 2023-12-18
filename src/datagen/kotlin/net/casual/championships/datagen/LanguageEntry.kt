package net.casual.championships.datagen

data class LanguageEntry(
    val key: String,
    val translation: String
) {
    override fun toString(): String {
        return """"${this.key}": "${this.translation}""""
    }

    companion object {
        fun toJson(entries: Collection<LanguageEntry>): String {
            val builder = StringBuilder()
            builder.append("{\n")
            val iterator = entries.iterator()
            for (entry in iterator) {
                builder.append("  ").append(entry.toString())
                if (iterator.hasNext()) {
                    builder.append(",\n")
                }
            }
            builder.append("\n}")
            return builder.toString()
        }
    }
}