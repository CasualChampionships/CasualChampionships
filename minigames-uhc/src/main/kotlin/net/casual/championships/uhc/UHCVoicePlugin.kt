package net.casual.championships.uhc

import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatServerApi
import de.maxhenkel.voicechat.api.events.EventRegistration
import de.maxhenkel.voicechat.api.events.PlayerConnectedEvent
import net.casual.arcade.events.GlobalEventHandler
import net.casual.championships.uhc.event.VoiceChatPlayerConnectedEvent

class UHCVoicePlugin : VoicechatPlugin {
    override fun getPluginId(): String {
        return UHCMod.MOD_ID
    }

    override fun initialize(api: VoicechatApi?) {
        voicechatApi = api as VoicechatServerApi
        super.initialize(api)
    }

    override fun registerEvents(registration: EventRegistration?) {
        registration?.registerEvent(PlayerConnectedEvent::class.java) { event ->
            GlobalEventHandler.Server.broadcast(VoiceChatPlayerConnectedEvent(event.connection))
        }
        super.registerEvents(registration)
    }

    companion object {
        var voicechatApi: VoicechatServerApi? = null
    }
}
