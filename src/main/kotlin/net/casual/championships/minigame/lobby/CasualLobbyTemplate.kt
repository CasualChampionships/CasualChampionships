package net.casual.championships.minigame.lobby

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.lobby.Lobby
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.minigame.events.lobby.templates.SimpleLobbyTemplate
import net.casual.arcade.utils.location.template.LocationTemplate
import net.casual.arcade.utils.serialization.CodecProvider
import net.casual.championships.CasualMod
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

class CasualLobbyTemplate(
    area: PlaceableAreaTemplate,
    spawn: LocationTemplate,
    countdown: CountdownTemplate,
    bossbar: TimerBossBarTemplate,
    val podium: LocationTemplate,
    val podiumView: LocationTemplate,
    val fireworkLocations: List<LocationTemplate>
): SimpleLobbyTemplate(area, spawn, countdown, bossbar) {
    override fun create(level: ServerLevel): Lobby {
        val area = this.area.create(level)

        return CasualLobby(area, level, this.spawn, this.bossbar, this.countdown, this.podium, this.podiumView, this.fireworkLocations)
    }

    override fun codec(): MapCodec<out LobbyTemplate> {
        return CODEC
    }

    companion object: CodecProvider<CasualLobbyTemplate> {
        override val ID: ResourceLocation = CasualMod.id("lobby")

        override val CODEC: MapCodec<out CasualLobbyTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                PlaceableAreaTemplate.CODEC.fieldOf("area").forGetter(SimpleLobbyTemplate::area),
                LocationTemplate.CODEC.fieldOf("spawn").forGetter(SimpleLobbyTemplate::spawn),
                CountdownTemplate.CODEC.fieldOf("countdown").forGetter(SimpleLobbyTemplate::countdown),
                TimerBossBarTemplate.CODEC.fieldOf("bossbar").forGetter(SimpleLobbyTemplate::bossbar),
                LocationTemplate.CODEC.fieldOf("podium").forGetter(CasualLobbyTemplate::podium),
                LocationTemplate.CODEC.fieldOf("podium_view").forGetter(CasualLobbyTemplate::podiumView),
                LocationTemplate.CODEC.listOf().fieldOf("firework_locations").forGetter(CasualLobbyTemplate::fireworkLocations)
            ).apply(instance, ::CasualLobbyTemplate)
        }
    }
}