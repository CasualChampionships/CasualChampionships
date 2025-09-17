package net.casual.championships.minigame.event

import com.mojang.serialization.Codec
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.utils.serialization.codec.OrderedRecordCodecBuilder
import net.casual.championships.uhc.minigame.UHCMinigameFactory

data class EventConfiguration(
    val name: String = "default",
    val minigame: MinigameFactory = UHCMinigameFactory.DEFAULT,
    val packs: List<String> = listOf(),
    val operators: List<String> = listOf(),
    val lobby: String = "default"
) {
    companion object {
        val CODEC: Codec<EventConfiguration> = OrderedRecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("name").forGetter(EventConfiguration::name),
                MinigameFactory.CODEC.fieldOf("minigame").forGetter(EventConfiguration::minigame),
                Codec.STRING.listOf().fieldOf("additional_packs").forGetter(EventConfiguration::packs),
                Codec.STRING.listOf().fieldOf("operators").forGetter(EventConfiguration::operators),
                Codec.STRING.fieldOf("lobby").forGetter(EventConfiguration::lobby)
            ).apply(instance, ::EventConfiguration)
        }
    }
}