package net.casualuhc.uhcmod.uhc.handlers

import net.casualuhc.uhcmod.util.Texts
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.BossEvent

interface BossBarHandler {
    fun getTitle(): MutableComponent {
        return Texts.LOBBY_WELCOME.append(" ").append(Texts.CASUAL_UHC)
    }

    fun getColour(): BossEvent.BossBarColor {
        return BossEvent.BossBarColor.BLUE
    }
}