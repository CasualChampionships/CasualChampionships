package net.casual.championships.duel

import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO:
//  - Translations
object CasualDuelMod {
    const val MOD_ID = "casual_duel"

    val logger: Logger = LoggerFactory.getLogger("Casual")

    fun id(path: String): ResourceLocation {
        return ResourceLocation(MOD_ID, path)
    }
}