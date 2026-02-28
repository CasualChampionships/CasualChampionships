package net.casual.championships.duel.utils

import net.casual.arcade.resources.font.spacing.SpacingFontResources
import net.casual.arcade.resources.utils.withMiniFont
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.casual.arcade.visuals.ready.chat.ChatReadyBroadcaster
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

class DuelRequester(
    private val requester: ServerPlayer,
    players: Iterable<ServerPlayer>
): ChatReadyBroadcaster<ServerPlayer>(::unicast, multicast(players)) {
    private val accepted = HashSet<ServerGamePacketListenerImpl>()

    init {
        this.accepted.add(this.requester.connection)
    }

    override fun getBroadcastComponent(
        yes: Component,
        no: Component
    ): Component {
        return Component.empty()
            .append(Component.translatable("casual.duel.challenge", this.requester.displayName))
            .append(SpacingFontResources.spaced(4))
            .append(yes)
            .append(SpacingFontResources.spaced(4))
            .append(no)
            .withMiniFont()
    }

    override fun getYesComponent(): MutableComponent {
        return Component.literal("[")
            .append(Component.translatable("casual.duel.challenge.accept"))
            .append("]").lime()
    }

    override fun getNoComponent(): MutableComponent {
        return Component.literal("[")
            .append(Component.translatable("casual.duel.challenge.decline"))
            .append("]")
            .red()
    }

    override fun broadcastParticipantReady(participant: ServerPlayer) {
        this.multicast.invoke(Component.translatable("casual.duel.accepted", participant.displayName).lime().withMiniFont())
        this.accepted.add(participant.connection)
    }

    override fun broadcastParticipantNotReady(participant: ServerPlayer) {
        this.multicast.invoke(Component.translatable("casual.duel.declined", participant.displayName).red().withMiniFont())
        this.accepted.remove(participant.connection)
    }

    override fun broadcastSuccess() {

    }

    override fun broadcastFailure() {

    }

    fun broadcastTo(player: ServerPlayer, message: Component) {
        this.unicast.invoke(player, message)
    }

    fun getAccepted(): List<ServerPlayer> {
        return this.accepted.map { connection -> connection.player }
    }

    companion object {
        val DUEL_PREFIX = Component.literal("[⚔]").lime()

        private fun multicast(players: Iterable<ServerPlayer>): (Component) -> Unit {
            return { message ->
                for (player in players) {
                    this.unicast(player, message)
                }
            }
        }

        private fun unicast(player: ServerPlayer, message: Component) {
            player.sendSystemMessage(Component.empty().append(DUEL_PREFIX).append(" ").append(message))
        }
    }
}