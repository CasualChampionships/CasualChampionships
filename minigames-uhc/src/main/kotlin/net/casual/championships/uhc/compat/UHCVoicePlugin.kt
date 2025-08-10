package net.casual.championships.uhc.compat

import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatServerApi
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent
import net.casual.arcade.events.GlobalEventHandler
import net.casual.championships.uhc.UHCMod
import net.casual.championships.uhc.event.VoiceChatPlayerConnectedEvent

object UHCVoicePlugin: VoicechatPlugin {
    var voicechatApi: VoicechatServerApi? = null

    override fun getPluginId(): String {
        return UHCMod.MOD_ID
    }

    override fun initialize(api: VoicechatApi) {
        voicechatApi = api as VoicechatServerApi
    }

    override fun registerEvents(registration: EventRegistration) {
        registration.registerEvent(PlayerConnectedEvent::class.java) { event ->
            GlobalEventHandler.Server.broadcast(VoiceChatPlayerConnectedEvent(event.connection))
        }
    }
}
