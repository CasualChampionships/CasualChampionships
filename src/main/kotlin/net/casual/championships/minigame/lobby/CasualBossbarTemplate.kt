package net.casual.championships.minigame.lobby

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.template.bossbar.TimerBossbarTemplate
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.visuals.bossbar.TimerBossbar
import net.casual.championships.CasualMod
import net.casual.championships.common.ui.bossbar.LobbyBossbar
import net.minecraft.resources.ResourceLocation

class CasualBossbarTemplate: TimerBossbarTemplate {
    override fun create(): TimerBossbar {
        return LobbyBossbar()
    }

    override fun codec(): MapCodec<out TimerBossbarTemplate> {
        return CODEC
    }

    companion object: CodecProvider<CasualBossbarTemplate> {
        override val ID: ResourceLocation = CasualMod.id("bossbar")

        override val CODEC: MapCodec<out CasualBossbarTemplate> = MapCodec.unit(::CasualBossbarTemplate)
    }
}