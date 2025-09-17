package net.casual.championships.minigame.event

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.UUIDUtil
import java.util.*

class EventState(val minigameUUID: Optional<UUID>) {
    companion object {
        val CODEC: Codec<EventState> = RecordCodecBuilder.create { instance ->
            instance.group(
                UUIDUtil.STRING_CODEC.optionalFieldOf("minigame_uuid").forGetter(EventState::minigameUUID)
            ).apply(instance, ::EventState)
        }
    }
}