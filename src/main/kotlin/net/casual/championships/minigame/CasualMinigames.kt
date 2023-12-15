package net.casual.championships.minigame

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.gui.suppliers.ComponentSupplier
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.utils.ComponentUtils.aqua
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.gold
import net.casual.arcade.utils.FantasyUtils
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.LevelUtils
import net.casual.arcade.utils.MinigameUtils.getMinigame
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TickUtils
import net.casual.championships.minigame.uhc.UHCMinigame
import net.casual.championships.minigame.uhc.events.UHCEvents
import net.casual.championships.util.CasualUtils
import net.casual.championships.util.Config
import net.casual.championships.util.Texts
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import java.util.*

object CasualMinigames {
    private var uuid: String? by Config.stringOrNull("current_minigame_uuid")
    private var minigame: Minigame<*>? = null

    fun getCurrent(): Minigame<*> {
        return minigame!!
    }

    fun setLobby(server: MinecraftServer, next: Minigame<*>? = null): CasualLobbyMinigame {
        val lobby = CasualLobbyMinigame(server, UHCEvents.getUHC().getLobby())
        setNewMinigameAndStart(lobby)
        if (next != null) {
            lobby.setNextMinigame(next)
        }
        return lobby
    }

    fun setNewMinigameAndStart(minigame: Minigame<*>) {
        val current = CasualMinigames.minigame
        CasualMinigames.minigame = minigame
        uuid = minigame.uuid.toString()
        if (current != null) {
            for (player in current.getAllPlayers()) {
                if (current.isAdmin(player)) {
                    minigame.makeAdmin(player)
                }
                minigame.addPlayer(player)
            }
            current.close()
        }
        minigame.start()
    }

    internal fun registerEvents() {
        Minigames.registerFactory(CasualUtils.id("uhc_minigame"), this::createUHCMinigame)

        GlobalEventHandler.register<ServerLoadedEvent>(Int.MAX_VALUE) {
            val uuid = uuid
            if (uuid != null) {
                val current = Minigames.get(UUID.fromString(uuid))
                minigame = current
                if (current != null) {
                    return@register
                }
            }

            setLobby(it.server)
        }

        GlobalEventHandler.register<PlayerJoinEvent> {
            val player = it.player
            if (player.getMinigame() == null) {
                val current = getCurrent()
                current.addPlayer(player)
                if (Config.operators.contains(player.scoreboardName)) {
                    current.makeAdmin(player)
                }
            }
        }
    }

    fun createTabDisplay(): ArcadeTabDisplay {
        val display = ArcadeTabDisplay(
            ComponentSupplier.of(
                Component.literal("\n")
                    .append(Texts.ICON_UHC)
                    .append(Texts.space())
                    .append(Texts.CASUAL_UHC.gold().bold())
                    .append(Texts.space())
                    .append(Texts.ICON_UHC)
            )
        ) { _ ->
            val tps = TickUtils.calculateTPS()
            val formatting = if (tps >= 20) ChatFormatting.DARK_GREEN else if (tps > 15) ChatFormatting.YELLOW else if (tps > 10) ChatFormatting.RED else ChatFormatting.DARK_RED
            Component.literal("\n")
                .append("TPS: ")
                .append(Component.literal("%.1f".format(tps)).withStyle(formatting))
                .append("\n")
                .append(Texts.TAB_HOSTED.aqua().bold())
        }
        return display
    }

    private fun createUHCMinigame(context: MinigameCreationContext): UHCMinigame {
        val overworldConfig = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(LevelUtils.overworld().chunkSource.generator)
        val netherConfig = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.NETHER)
            .setGenerator(LevelUtils.nether().chunkSource.generator)
        val endConfig = RuntimeWorldConfig()
            .setDimensionType(BuiltinDimensionTypes.END)
            .setGenerator(LevelUtils.end().chunkSource.generator)

        val overworldId: ResourceLocation
        val netherId: ResourceLocation
        val endId: ResourceLocation
        if (context.hasCustomData()) {
            val dimensions = context.getCustomData().obj("dimensions")
            overworldId = ResourceLocation(dimensions.string("overworld"))
            netherId = ResourceLocation(dimensions.string("nether"))
            endId = ResourceLocation(dimensions.string("end"))
        } else {
            overworldId = ResourceUtils.random()
            netherId = ResourceUtils.random()
            endId = ResourceUtils.random()
        }

        val (overworld, nether, end) = FantasyUtils.createPersistentVanillaLikeLevels(
            overworldConfig = FantasyUtils.PersistentConfig(overworldId, overworldConfig),
            netherConfig = FantasyUtils.PersistentConfig(netherId, netherConfig),
            endConfig = FantasyUtils.PersistentConfig(endId, endConfig)
        )
        return UHCMinigame(
            server = context.server,
            overworld = overworld,
            nether = nether,
            end = end
        )
    }
}