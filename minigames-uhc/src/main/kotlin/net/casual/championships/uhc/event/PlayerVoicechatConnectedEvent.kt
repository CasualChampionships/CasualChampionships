package net.casual.championships.uhc.event

import de.maxhenkel.voicechat.api.VoicechatConnection
import net.casual.arcade.events.server.player.PlayerEvent
import net.minecraft.server.level.ServerPlayer

data class PlayerVoicechatConnectedEvent(
    override val player: ServerPlayer,
    val connection: VoicechatConnection
): PlayerEvent
