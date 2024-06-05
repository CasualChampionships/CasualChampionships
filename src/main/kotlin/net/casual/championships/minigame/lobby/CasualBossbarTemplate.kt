package net.casual.championships.minigame.lobby

import com.mojang.serialization.MapCodec
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.utils.serialization.CodecProvider
import net.casual.championships.CasualMod
import net.casual.championships.common.ui.bossbar.LobbyBossBar
import net.minecraft.resources.ResourceLocation

class CasualBossbarTemplate: TimerBossBarTemplate {
    override fun create(): TimerBossBar {
        return LobbyBossBar()
    }

    override fun codec(): MapCodec<out TimerBossBarTemplate> {
        return CODEC
    }

    companion object: CodecProvider<CasualBossbarTemplate> {
        override val ID: ResourceLocation = CasualMod.id("bossbar")

        override val CODEC: MapCodec<out CasualBossbarTemplate> = MapCodec.unit(::CasualBossbarTemplate)
    }
}