package net.casual.championships.uhc.event

import de.maxhenkel.voicechat.api.VoicechatConnection
import net.casual.arcade.events.common.Event

data class VoiceChatPlayerConnectedEvent(val connection: VoicechatConnection): Event
