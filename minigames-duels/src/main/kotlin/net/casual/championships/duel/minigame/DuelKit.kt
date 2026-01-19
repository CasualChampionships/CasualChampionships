package net.casual.championships.duel.minigame

import net.casual.arcade.utils.convertCasing
import net.casual.arcade.utils.string.PascalCase
import net.casual.arcade.utils.string.TitleCase

enum class DuelKit {
    RandomGear, MaceCharged;

    fun getFancyName(): String {
        return this.name.convertCasing(PascalCase, TitleCase)
    }
}