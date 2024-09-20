package net.casual.championships.minigame.lobby

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.template.countdown.CountdownTemplate
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.visuals.countdown.Countdown
import net.casual.championships.CasualMod
import net.casual.championships.common.ui.CasualCountdown
import net.minecraft.resources.ResourceLocation

class CasualCountdownTemplate: CountdownTemplate {
    override fun create(): Countdown {
        return CasualCountdown
    }

    override fun codec(): MapCodec<out CountdownTemplate> {
        return CODEC
    }

    companion object: CodecProvider<CasualCountdownTemplate> {
        override val ID: ResourceLocation = CasualMod.id("countdown")

        override val CODEC: MapCodec<out CasualCountdownTemplate> = MapCodec.unit(::CasualCountdownTemplate)
    }
}