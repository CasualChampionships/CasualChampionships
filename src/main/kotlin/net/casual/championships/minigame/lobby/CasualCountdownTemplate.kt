package net.casual.championships.minigame.lobby

import com.mojang.serialization.Codec
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.utils.serialization.CodecProvider
import net.casual.championships.CasualMod
import net.casual.championships.common.ui.CasualCountdown
import net.minecraft.resources.ResourceLocation

class CasualCountdownTemplate: CountdownTemplate {
    override fun create(): Countdown {
        return CasualCountdown
    }

    override fun codec(): Codec<out CountdownTemplate> {
        return CODEC
    }

    companion object: CodecProvider<CasualCountdownTemplate> {
        override val ID: ResourceLocation = CasualMod.id("countdown")

        override val CODEC: Codec<out CasualCountdownTemplate> = Codec.unit(::CasualCountdownTemplate)
    }
}