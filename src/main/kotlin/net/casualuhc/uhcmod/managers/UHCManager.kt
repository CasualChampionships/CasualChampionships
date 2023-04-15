package net.casualuhc.uhcmod.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.core.Event
import net.casualuhc.arcade.events.player.*
import net.casualuhc.arcade.events.server.ServerLoadedEvent
import net.casualuhc.arcade.events.server.ServerRecipeReloadEvent
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.recipes.CraftingRecipeBuilder
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.Seconds
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.casualuhc.arcade.scheduler.Task
import net.casualuhc.arcade.scheduler.TaskScheduler
import net.casualuhc.arcade.utils.ComponentUtils.bold
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.casualuhc.arcade.utils.ComponentUtils.lime
import net.casualuhc.arcade.utils.ComponentUtils.red
import net.casualuhc.arcade.utils.LevelUtils
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casualuhc.arcade.utils.PlayerUtils.grantAdvancement
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.arcade.utils.PlayerUtils.sendSound
import net.casualuhc.arcade.utils.PlayerUtils.sendTitle
import net.casualuhc.arcade.utils.TeamUtils.getServerPlayers
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.advancement.RaceAdvancement
import net.casualuhc.uhcmod.advancement.UHCAdvancements
import net.casualuhc.uhcmod.events.uhc.*
import net.casualuhc.uhcmod.extensions.TeamFlag.Ignored
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.managers.PlayerManager.belongsToTeam
import net.casualuhc.uhcmod.managers.PlayerManager.sendUHCResourcePack
import net.casualuhc.uhcmod.managers.PlayerManager.setForUHC
import net.casualuhc.uhcmod.managers.UHCManager.Phase.*
import net.casualuhc.uhcmod.recipes.GoldenHeadRecipe
import net.casualuhc.uhcmod.settings.GameSettings
import net.casualuhc.uhcmod.task.SavableTask
import net.casualuhc.uhcmod.uhc.DefaultUHC
import net.casualuhc.uhcmod.uhc.UHCEvent
import net.casualuhc.uhcmod.uhc.UHCEvents
import net.casualuhc.uhcmod.util.Config
import net.casualuhc.uhcmod.util.HeadUtils
import net.casualuhc.uhcmod.util.RuleUtils
import net.casualuhc.uhcmod.util.Texts
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.bossevents.CustomBossEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import java.nio.file.Files
import java.util.*
import kotlin.io.path.exists

object UHCManager {
    private val configPath = Config.resolve("manager.json")
    private val claimed = HashSet<RaceAdvancement>()
    private val scheduler = TaskScheduler()

    private var bossBar: CustomBossEvent? = null
    private var startTime = Long.MAX_VALUE

    private var glowing = false

    lateinit var event: UHCEvent
        private set

    var phase: Phase = Setup
        private set
    var uptime = 0
        private set

    fun isPhase(phase: Phase): Boolean {
        return this.phase == phase
    }

    fun setPhase(phase: Phase) {
        this.scheduler.tasks.clear()
        this.phase = phase

        EventHandler.broadcast(phase.eventFactory())
    }

    fun schedulePhaseTask(time: Int, unit: MinecraftTimeUnit, task: Task) {
        this.scheduler.schedule(time, unit, task)
    }

    fun schedulePhaseTask(time: Int, unit: MinecraftTimeUnit, runnable: Runnable) {
        this.scheduler.schedule(time, unit, runnable)
    }

    fun scheduleInLoopPhaseTask(delay: Int, interval: Int, duration: Int, unit: MinecraftTimeUnit, runnable: Runnable) {
        this.scheduler.scheduleInLoop(delay, interval, duration, unit, runnable)
    }

    fun isReadyForPlayers(): Boolean {
        return this.phase >= Lobby && GameSettings.FLOODGATE.value
    }

    fun hasUHCStarted(): Boolean {
        return this.phase >= Start
    }

    fun isLobbyPhase(): Boolean {
        return this.isPhase(Lobby)
    }

    fun isReadyPhase(): Boolean {
        return this.isPhase(Ready)
    }

    fun isActivePhase(): Boolean {
        return this.isPhase(Active)
    }

    fun setStartTime(newStartTime: Long) {
        this.startTime = newStartTime
        if (this.startTime - 30 * 60 * 1000 >= System.currentTimeMillis()) {
            this.generateBossBar()
        }
    }

    fun isUnclaimed(achievement: RaceAdvancement): Boolean {
        return this.claimed.add(achievement)
    }

    fun resetTrackers() {
        this.uptime = 0
        this.glowing = false
        this.claimed.clear()
    }

    internal fun registerEvents() {
        EventHandler.register<UHCLoadTasksEvent> { it.add(BorderEndTask()).add(GracePeriodOverTask()) }
        EventHandler.register<UHCConfigLoadedEvent>(0) { this.onConfigLoaded() }
        EventHandler.register<ServerLoadedEvent> { this.onServerLoaded() }
        EventHandler.register<ServerStoppedEvent> { this.onServerStopped() }
        EventHandler.register<ServerTickEvent> { this.onServerTick() }
        EventHandler.register<PlayerJoinEvent> { this.onPlayerJoin(it) }
        EventHandler.register<PlayerLeaveEvent> { this.onPlayerLeave(it) }
        EventHandler.register<ServerRecipeReloadEvent> { this.onRecipeReload(it) }

        EventHandler.register<UHCSetupEvent> { this.onUHCSetup() }
        EventHandler.register<UHCLobbyEvent> { this.onUHCLobby() }
        EventHandler.register<UHCStartEvent> { this.onUHCStart() }
        EventHandler.register<UHCActiveEvent> { this.onUHCActive() }
        EventHandler.register<UHCEndEvent> { this.onUHCEnd() }
        EventHandler.register<UHCBorderCompleteEvent> { this.onWorldBorderComplete() }
    }

    private fun onConfigLoaded() {
        this.event = UHCEvents.getUHC(Config.string("event")) ?: DefaultUHC.INSTANCE
        this.event.load()
    }

    private fun onServerLoaded() {
        if (!this.configPath.exists()) {
            return
        }
        val json = Files.newBufferedReader(this.configPath).use {
            Gson().fromJson(it, JsonObject::class.java)
        }
        // Do not call this.setPhase, prevents event broadcasting
        this.phase = Phase.valueOf(json["phase"].asString)
        this.glowing = json["glowing"].asBoolean

        val tasks = UHCLoadTasksEvent()
        EventHandler.broadcast(tasks)

        for (element in json.getAsJsonArray("tasks")) {
            val delay = (element as JsonObject)["delay"].asInt
            val name = element["name"].asString
            val task = tasks.get(name)

            if (task === null) {
                UHCMod.logger.warn("Saved task $name could not be reloaded!")
                continue
            } else {
                UHCMod.logger.info("Successfully loaded task $name, scheduled for $delay ticks")
            }
            this.schedulePhaseTask(delay, Ticks, task)
        }

        if (this.isActivePhase()) {
            this.uptime = json["uptime"].asInt
            for (claimed in json.getAsJsonArray("claimed")) {
                this.claimed.add(RaceAdvancement.valueOf(claimed.asString))
            }
        }
    }

    private fun onServerStopped() {
        val json = JsonObject()
        json.addProperty("phase", this.phase.name)
        json.addProperty("glowing", this.glowing)

        val tasks = JsonArray()
        for ((tick, queue) in this.scheduler.tasks) {
            val delay = tick - this.scheduler.tickCount
            for (task in queue) {
                if (task is SavableTask) {
                    val element = JsonObject()
                    element.addProperty("delay", delay)
                    element.addProperty("name", task.id)
                    tasks.add(element)
                }
            }
        }
        json.add("tasks", tasks)

        if (this.isActivePhase()) {
            json.addProperty("uptime", this.uptime)
            val claimed = JsonArray()
            for (advancement in this.claimed) {
                claimed.add(advancement.name)
            }
            json.add("claimed", claimed)
        }

        val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
        Files.writeString(this.configPath, gson.toJson(json))
    }

    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        val bar = this.bossBar
        if (bar != null && this.isLobbyPhase()) {
            bar.addPlayer(player)
        }
        if (this.isActivePhase() && this.glowing && player.isSurvival) {
            player.setGlowingTag(true)
        }
    }

    private fun onPlayerLeave(event: PlayerLeaveEvent) {
        DataManager.database.updateStats(event.player)
    }

    private fun onRecipeReload(event: ServerRecipeReloadEvent) {
        event.add(GoldenHeadRecipe())
    }

    private fun onServerTick() {
        this.scheduler.tick()

        this.uptime++
        val bar = this.bossBar
        if (!this.isLobbyPhase() || bar == null) {
            return
        }
        if (this.uptime % 100 == 0 && this.startTime - 30 * 60 * 1000 < System.currentTimeMillis()) {
            val millisLeft = startTime - System.currentTimeMillis()
            val percentLeft = millisLeft / (30 * 60 * 1000).toFloat()
            bar.progress = Mth.clamp(1 - percentLeft, 0.0F, 1.0F)
            val minutesLeft = millisLeft / (60 * 1000)
            val startTime = when {
                minutesLeft <= 0 -> Texts.LOBBY_STARTING_SOON
                minutesLeft == 1L -> Texts.LOBBY_STARTING_ONE
                else -> Texts.LOBBY_STARTING_GENERIC.generate(minutesLeft)
            }.lime().bold()
            bar.name = this.event.getBossBarHandler().getTitle().append(". ").append(startTime)
        }
    }

    private fun onUHCSetup() {
        this.deleteAllBossBars()
    }

    private fun onUHCLobby() {
        RuleUtils.setLobbyGamerules()

        val handler = this.event.getLobbyHandler()
        handler.place()
        PlayerUtils.forEveryPlayer { player ->
            handler.forceTeleport(player)
        }
        this.generateBossBar()
    }

    private fun onUHCStart() {
        this.setStartTime(Long.MAX_VALUE)
        this.hideBossBar()
        this.deleteAllBossBars()

        // Countdown
        var starting = 10
        this.scheduleInLoopPhaseTask(0, 1, 10, Seconds) {
            val title = Component.literal(starting--.toString()).lime()
            PlayerUtils.forEveryPlayer { player ->
                player.sendTitle(title)
                player.sendSound(SoundEvents.NOTE_BLOCK_PLING.value(), pitch = 3.0F)
            }
        }
        this.schedulePhaseTask(10, Seconds) {
            PlayerUtils.forEveryPlayer { player ->
                player.setForUHC(true)
                player.sendUHCResourcePack()
            }

            this.event.getLobbyHandler().remove()

            this.setPhase(Active)
            val players = PlayerUtils.players().filter { player ->
                val team = player.team
                team !== null && !team.flags.has(Ignored)
            }
            PlayerUtils.spread(LevelUtils.overworld(), Vec2(0.0F, 0.0F), 500.0, 2900.0, true, players)
        }
    }

    private fun onUHCActive() {
        this.resetTrackers()
        RuleUtils.setActiveGamerules()

        PlayerUtils.forEveryPlayer { player ->
            player.sendSystemMessage(Texts.UHC_GRACE_FIRST.gold())
            player.sendSound(SoundEvents.NOTE_BLOCK_PLING.value())
        }
        var minutes = 10
        this.scheduleInLoopPhaseTask(2, 2, 8, MinecraftTimeUnit.Minutes) {
            minutes -= 2
            val message = Texts.UHC_GRACE_GENERIC.generate(minutes).gold()
            PlayerUtils.forEveryPlayer { player ->
                player.sendSystemMessage(message)
                player.sendSound(SoundEvents.NOTE_BLOCK_PLING.value())
            }
        }
        this.schedulePhaseTask(10, MinecraftTimeUnit.Minutes, GracePeriodOverTask())
    }

    private fun onUHCEnd() {
        val team = TeamManager.getAnyAliveTeam()
        if (team == null) {
            UHCMod.logger.error("Last team was null!")
            return
        }
        val alive = team.getServerPlayers().filter { it.isSurvival }
        if (alive.size == 1) {
            alive[0].grantAdvancement(UHCAdvancements.LAST_MAN_STANDING)
        }

        for (player in alive) {
            player.setGameMode(GameType.ADVENTURE)
            player.abilities.mayfly = true
            player.abilities.invulnerable = true
            player.onUpdateAbilities()
        }

        PlayerUtils.forEveryPlayer { player ->
            if (player.belongsToTeam(team)) {
                player.grantAdvancement(UHCAdvancements.WINNER)
            }
            player.setGlowingTag(false)
            player.sendTitle(Texts.UHC_WON.generate(team.name).withStyle(team.color))

            DataManager.database.updateStats(player)
        }

        DataManager.database.incrementTeamWin(team)
        DataManager.database.combineStats()

        this.scheduleInLoopPhaseTask(0, 4, 100, Ticks) {
            PlayerUtils.forEveryPlayer { player ->
                player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST, volume = 0.5F)
                player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                player.sendSound(SoundEvents.FIREWORK_ROCKET_BLAST_FAR, volume = 0.5F)
            }
            this.schedulePhaseTask(6, Ticks) {
                PlayerUtils.forEveryPlayer { player ->
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_SHOOT, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, volume = 0.5F)
                    player.sendSound(SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR, volume = 0.5F)
                }
            }
        }


        this.schedulePhaseTask(2, Ticks) {
            PlayerUtils.forEveryPlayer { it.clearPlayerInventory() }
            this.setPhase(Lobby)
        }
    }

    private fun onWorldBorderComplete() {
        UHCMod.logger.info("World Border Completed")
        this.schedulePhaseTask(5, MinecraftTimeUnit.Minutes, BorderEndTask())
    }

    private fun generateBossBar() {
        val bar: CustomBossEvent
        val handler = this.event.getBossBarHandler()
        if (this.bossBar == null) {
            val manager = Arcade.server.customBossEvents
            val id = ResourceLocation("uhc", "lobby")
            bar = manager.get(id) ?: manager.create(id, handler.getTitle())
            this.bossBar = bar
        } else {
            bar = this.bossBar!!
            bar.name = handler.getTitle()
        }
        bar.progress = 1.0F
        bar.color = handler.getColour()
        PlayerUtils.forEveryPlayer(bar::addPlayer)
    }

    private fun hideBossBar() {
        this.bossBar?.removeAllPlayers()
    }

    private fun deleteAllBossBars() {
        val manager = Arcade.server.customBossEvents
        for (bar in LinkedList(manager.events)) {
            bar.removeAllPlayers()
            manager.remove(bar)
        }
    }

    enum class Phase(internal val eventFactory: () -> Event) {
        Setup(::UHCSetupEvent),
        Lobby(::UHCLobbyEvent),
        Ready(::UHCReadyEvent),
        Start(::UHCStartEvent),
        Active(::UHCActiveEvent),
        End(::UHCEndEvent)
    }

    private class GracePeriodOverTask: SavableTask() {
        override val id = "grace_period_over_task"

        override fun run() {
            val message = Texts.UHC_GRACE_OVER.red().bold()
            PlayerUtils.forEveryPlayer { player ->
                player.sendSystemMessage(message)
                player.sendSound(SoundEvents.ENDER_DRAGON_AMBIENT)
            }
            GameSettings.PVP.setValue(true)

            EventHandler.broadcast(UHCGracePeriodEndEvent())
        }
    }

    private class BorderEndTask: SavableTask() {
        override val id = "waiting_border_end_task"

        override fun run() {
            if (GameSettings.END_GAME_GLOW.value) {
                glowing = true
                var count = 0
                PlayerUtils.forEveryPlayer { player ->
                    if (player.isSurvival) {
                        player.setGlowingTag(true)
                        count++
                    }
                }
                UHCMod.logger.info("$count player's are now glowing")
            }
            if (GameSettings.GENERATE_PORTAL.value) {
                LevelUtils.overworld().portalForcer.createPortal(BlockPos.ZERO, Direction.Axis.X)
                LevelUtils.nether().portalForcer.createPortal(BlockPos.ZERO, Direction.Axis.X)
            }
        }
    }
}
