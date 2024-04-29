package net.casual.championships.minigame.lobby

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.lobby.*
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.minigame.events.lobby.templates.SimpleLobbyTemplate
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.serialization.CodecProvider
import net.casual.championships.CasualMod
import net.casual.championships.minigame.CasualMinigames
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

class CasualLobbyTemplate(
    area: PlaceableAreaTemplate,
    spawn: LocationTemplate,
    countdown: CountdownTemplate,
    bossbar: TimerBossBarTemplate,
    val podium: LocationTemplate
): SimpleLobbyTemplate(area, spawn, countdown, bossbar) {
    override fun create(level: ServerLevel): Lobby {
        val area = this.area.create(level)
        val spawn = this.spawn.toLocation(level)
        val podium = this.podium.toLocation(level)

        return object: Lobby {
            override val area: PlaceableArea = area
            override val spawn: Location = spawn

            override fun getSpawn(player: ServerPlayer): Location {
                if (CasualMinigames.isWinner(player)) {
                    return podium
                }
                return super.getSpawn(player)
            }

            override fun createBossbar(): TimerBossBar {
                return bossbar.create()
            }

            override fun getCountdown(): Countdown {
                return countdown.create()
            }
        }
    }

    override fun codec(): Codec<out LobbyTemplate> {
        return CODEC
    }

    companion object: CodecProvider<CasualLobbyTemplate> {
        override val ID: ResourceLocation = CasualMod.id("lobby")

        override val CODEC: Codec<out CasualLobbyTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                PlaceableAreaTemplate.CODEC.fieldOf("area").forGetter(SimpleLobbyTemplate::area),
                LocationTemplate.CODEC.fieldOf("spawn").forGetter(SimpleLobbyTemplate::spawn),
                CountdownTemplate.CODEC.fieldOf("countdown").forGetter(SimpleLobbyTemplate::countdown),
                TimerBossBarTemplate.CODEC.fieldOf("bossbar").forGetter(SimpleLobbyTemplate::bossbar),
                LocationTemplate.CODEC.fieldOf("podium").forGetter(CasualLobbyTemplate::podium)
            ).apply(instance, ::CasualLobbyTemplate)
        }
    }
}