package net.casual.championships.missilewars

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.player.PlayerEvent
import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.gui.bossbar.StaticBossBar
import net.casual.arcade.gui.bossbar.SuppliedBossBar
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.elements.ComponentElements
import net.casual.arcade.gui.elements.LevelSpecificElement
import net.casual.arcade.gui.elements.OverlayElements
import net.casual.arcade.gui.elements.SidebarElements
import net.casual.arcade.gui.elements.UniversalElement
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.sidebar.SidebarComponent
import net.casual.arcade.gui.tab.ArcadePlayerListDisplay
import net.casual.arcade.gui.tab.PlayerListEntries
import net.casual.arcade.gui.tab.VanillaPlayerListEntries
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameSettings
import net.casual.arcade.minigame.annotation.During
import net.casual.arcade.minigame.annotation.Listener
import net.casual.arcade.minigame.phase.Phase
import net.casual.arcade.minigame.task.impl.BossBarTask
import net.casual.arcade.minigame.task.impl.PhaseChangeTask
import net.casual.arcade.settings.display.MenuGameSettingBuilder
import net.casual.arcade.utils.BossbarUtils
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.green
import net.casual.arcade.utils.ComponentUtils.italicise
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.purple
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.PlayerUtils.teleportTo
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.Location
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import kotlin.io.path.createDirectories

class MyPlayerEvent(
    override val player: ServerPlayer
): PlayerEvent

class MyEvent(
    val foo: String,
    val bar: Int
): CancellableEvent.Default() {

}

fun broadcastMyEvent() {
    val event = MyEvent("Foo", 10)
    GlobalEventHandler.broadcast(event, BuiltInEventPhases.PRE_PHASES)
    // Do Something
    GlobalEventHandler.broadcast(event, BuiltInEventPhases.POST_PHASES)

    if (event.isCancelled()) {

    }

}

class ExampleSettings(minigame: Minigame<*>): MinigameSettings(minigame) {
    val myCustomSetting by this.register(MenuGameSettingBuilder.bool {
        name = "my_setting"
        value = false
        display = Items.IRON_BLOCK.named("My Setting")

        defaults.options(this)
    })
}

class ExampleMod: ModInitializer {
    private lateinit var minigame: ExampleMinigame

    override fun onInitialize() {
        GlobalEventHandler.register<ServerLoadedEvent> { (server) ->
            minigame = ExampleMinigame(server)
        }
        GlobalEventHandler.register<PlayerJoinEvent> { (player) ->
            minigame.players.add(player, spectating = false)
        }
    }
}

class MyCustomTimerBossBar: TimerBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        TODO("Not yet implemented")
    }

    override fun getProgress(player: ServerPlayer): Float {
        return BossbarUtils.scale(this.getProgress(), 0.75F)
    }
// ...
}

class ExampleMinigame(server: MinecraftServer): Minigame<ExampleMinigame>(server) {
    override val id: ResourceLocation = ID

    override val settings: MinigameSettings = ExampleSettings(this)

    var bossbar: MyCustomTimerBossBar = MyCustomTimerBossBar()

    override fun getPhases(): Collection<Phase<ExampleMinigame>> {
        return listOf(ExamplePhases.Grace, ExamplePhases.Active, ExamplePhases.DeathMatch)
    }

    override fun initialize() {
        super.initialize()
        val tag = ArcadeNameTag({ player -> Component.literal("[CNT] ").append(player.displayName) })
        this.ui.addNameTag(tag)
        val tag2 = ArcadeNameTag({ player -> Component.literal("${player.health} ❤") })
        this.ui.addNameTag(tag2)

        val tag3 = ArcadeNameTag(
            ComponentElements.of(Component.literal("< 5 hearts!")),
            { observee, observer -> observer.isSpectator && observee.health < 10 }
        )
        this.ui.addNameTag(tag3)
    }

    @Listener(priority = 2_000, during = During(before = "grace", after = "death_match"))
    private fun onMinigameAddPlayer(event: MinigameAddPlayerEvent) {

    }

    companion object {
        val ID = ResourceLocation("modid", "example")
    }
}

class MyPlayerListEntries: PlayerListEntries {
    override val size: Int
        get() = TODO("Not yet implemented")

    override fun getEntryAt(index: Int): PlayerListEntries.Entry {
        TODO("Not yet implemented")
    }
}

enum class ExamplePhases(
    override val id: String
): Phase<ExampleMinigame> {
    Grace("grace") {
        override fun start(minigame: ExampleMinigame) {
            minigame.settings.canPvp.set(false)

            // In 10 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(10.Minutes, PhaseChangeTask(minigame, Active))
        }

        override fun initialize(minigame: ExampleMinigame) {
            val duration = 10.Minutes
            val bossbar = MyCustomTimerBossBar()
            bossbar.setDuration(duration)

            FabricLoader.getInstance().gameDir.resolve("resource-packs")
            val task = BossBarTask(minigame, bossbar)
            minigame.scheduler.schedulePhasedCancellable(duration + 1.Ticks, task).runIfCancelled()
        }
    },
    Active("active") {
        override fun start(minigame: ExampleMinigame) {
            minigame.settings.canPvp.set(true)

            // In 30 minutes we will move to the next phase
            minigame.scheduler.schedulePhased(30.Minutes, PhaseChangeTask(minigame, DeathMatch))
        }
    },
    DeathMatch("death_match") {
        override fun start(minigame: ExampleMinigame) {
            // Change to location of the arena
            val location = Location.of()
            for (player in minigame.players.playing) {
                player.teleportTo(location)
            }
        }
    }
}