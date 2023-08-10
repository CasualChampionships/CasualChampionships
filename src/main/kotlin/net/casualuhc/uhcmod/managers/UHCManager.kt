package net.casualuhc.uhcmod.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.core.Event
import net.casualuhc.arcade.events.player.PlayerDeathEvent
import net.casualuhc.arcade.events.player.PlayerJoinEvent
import net.casualuhc.arcade.events.player.PlayerLeaveEvent
import net.casualuhc.arcade.events.player.PlayerTickEvent
import net.casualuhc.arcade.events.server.ServerLoadedEvent
import net.casualuhc.arcade.events.server.ServerRecipeReloadEvent
import net.casualuhc.arcade.events.server.ServerStoppedEvent
import net.casualuhc.arcade.events.server.ServerTickEvent
import net.casualuhc.arcade.gui.ArcadeBossbar
import net.casualuhc.arcade.gui.ArcadeNameDisplay
import net.casualuhc.arcade.gui.ArcadeSidebar
import net.casualuhc.arcade.gui.suppliers.ComponentSupplier
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.*
import net.casualuhc.arcade.scheduler.Task
import net.casualuhc.arcade.scheduler.TickedScheduler
import net.casualuhc.arcade.utils.ComponentUtils.bold
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.casualuhc.arcade.utils.ComponentUtils.lime
import net.casualuhc.arcade.utils.ComponentUtils.red
import net.casualuhc.arcade.utils.LevelUtils
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casualuhc.arcade.utils.PlayerUtils.distanceToNearestBorder
import net.casualuhc.arcade.utils.PlayerUtils.grantAdvancement
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.arcade.utils.PlayerUtils.sendSound
import net.casualuhc.arcade.utils.PlayerUtils.sendTitle
import net.casualuhc.arcade.utils.TeamUtils
import net.casualuhc.arcade.utils.TeamUtils.getServerPlayers
import net.casualuhc.arcade.utils.TimeUtils
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.advancement.RaceAdvancement
import net.casualuhc.uhcmod.advancement.UHCAdvancements
import net.casualuhc.uhcmod.events.uhc.*
import net.casualuhc.uhcmod.extensions.PlayerFlag.Won
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.PlayerStat.Kills
import net.casualuhc.uhcmod.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casualuhc.uhcmod.extensions.PlayerUHCExtension.Companion.uhc
import net.casualuhc.uhcmod.extensions.TeamFlag.Ignored
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.TeamUHCExtension.Companion.uhc
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
import net.casualuhc.uhcmod.util.BossbarUtils
import net.casualuhc.uhcmod.util.Config
import net.casualuhc.uhcmod.util.RuleUtils
import net.casualuhc.uhcmod.util.Texts
import net.casualuhc.uhcmod.util.Texts.monospaced
import net.casualuhc.uhcmod.util.Texts.shadowless
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.Vec2
import java.nio.file.Files
import kotlin.io.path.exists

object UHCManager {
    private val configPath = Config.resolve("manager.json")
    private val claimed = HashSet<RaceAdvancement>()
    private val scheduler = TickedScheduler()
    private val bossbars = ArrayList<ArcadeBossbar>()

    private var sidebar: ArcadeSidebar? = null
    private var health: ArcadeNameDisplay? = null
    private var startTime = Long.MAX_VALUE

    private var glowing = false

    lateinit var event: UHCEvent
        private set

    var phase: Phase = Setup
        private set
    var uptime = 0
        private set

    var paused = false
        private set

    fun isPhase(phase: Phase): Boolean {
        return this.phase == phase
    }

    fun setPhase(phase: Phase) {
        this.scheduler.tasks.clear()
        this.phase = phase

        GlobalEventHandler.broadcast(phase.eventFactory())
    }

    fun pause() {
        this.paused = true
        if (this.isActivePhase()) {
            // Pause border
            WorldBorderManager.moveWorldBorders(WorldBorderManager.getGlobalBorder().size)
        }
    }

    fun unpause() {
        this.paused = false
        if (this.isActivePhase()) {
            WorldBorderManager.startWorldBorders()
        }
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
        GlobalEventHandler.register<UHCLoadTasksEvent> { it.add(BorderEndTask()).add(GracePeriodOverTask()) }
        GlobalEventHandler.register<UHCConfigLoadedEvent>(0) { this.onConfigLoaded() }
        GlobalEventHandler.register<ServerLoadedEvent> { this.onServerLoaded() }
        GlobalEventHandler.register<ServerStoppedEvent> { this.onServerStopped() }
        GlobalEventHandler.register<ServerTickEvent> { this.onServerTick() }
        GlobalEventHandler.register<PlayerJoinEvent> { this.onPlayerJoin(it) }
        GlobalEventHandler.register<PlayerTickEvent> { this.onPlayerTick(it) }
        GlobalEventHandler.register<PlayerDeathEvent> { this.onPlayerDeath(it) }
        GlobalEventHandler.register<PlayerLeaveEvent> { this.onPlayerLeave(it) }
        GlobalEventHandler.register<ServerRecipeReloadEvent> { this.onRecipeReload(it) }

        GlobalEventHandler.register<UHCSetupEvent> { this.onUHCSetup() }
        GlobalEventHandler.register<UHCLobbyEvent> { this.onUHCLobby() }
        GlobalEventHandler.register<UHCStartEvent> { this.onUHCStart() }
        GlobalEventHandler.register<UHCActiveEvent> { this.onUHCActive() }
        GlobalEventHandler.register<UHCEndEvent> { this.onUHCEnd() }
        GlobalEventHandler.register<UHCBorderCompleteEvent> { this.onWorldBorderComplete() }
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
        this.paused = json["paused"]?.asBoolean ?: false

        if (this.isActivePhase()) {
            this.createActiveSidebar()
            this.createActiveBossbar()
        }

        val tasks = UHCLoadTasksEvent()
        GlobalEventHandler.broadcast(tasks)

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
        json.addProperty("paused", this.paused)

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

        for (bossbar in this.bossbars) {
            bossbar.addPlayer(player)
        }

        this.sidebar?.addPlayer(player)

        if (this.isActivePhase() && this.glowing && player.isSurvival) {
            player.setGlowingTag(true)
        }

        if (this.isActivePhase() && player.isSpectator) {
            this.health?.addPlayer(player)
        }
    }

    private fun onPlayerTick(event: PlayerTickEvent) {
        if (this.isActivePhase()) {
            this.health?.setScore(event.player, (event.player.health / 2.0F).toInt())
        }
    }

    private fun onPlayerDeath(event: PlayerDeathEvent) {
        if (this.isActivePhase()) {
            this.health?.addPlayer(event.player)
        }
    }

    private fun onPlayerLeave(event: PlayerLeaveEvent) {
        DataManager.database.updateStats(event.player)
    }

    private fun onRecipeReload(event: ServerRecipeReloadEvent) {
        event.add(GoldenHeadRecipe())
    }

    private fun onServerTick() {
        if (this.paused) {
            return
        }

        this.scheduler.tick()

        this.uptime++
    }

    private fun onUHCSetup() {

    }

    private fun onUHCLobby() {
        this.removeBossbars()
        this.sidebar?.clearPlayers()
        this.sidebar = null
        this.health?.clearPlayers()
        this.health = null

        RuleUtils.setLobbyGamerules()

        val handler = this.event.getLobbyHandler()
        handler.getMap().place()

        PlayerUtils.forEveryPlayer { player ->
            if (!player.hasPermissions(4)) {
                player.setGameMode(GameType.ADVENTURE)
                handler.tryTeleport(player)
            }
        }
        TeamUtils.forEachTeam { team ->
            for (player in team.uhc.players) {
                Arcade.server.scoreboard.addPlayerToTeam(player, team)
            }
        }

        val start = ArcadeBossbar(
            title = {
                val time = when (this.startTime) {
                    Long.MAX_VALUE -> "99:99:99"
                    else -> {
                        if (this.startTime < 0) {
                            "00:00:00"
                        } else {
                            val secondsLeft = (this.startTime - System.currentTimeMillis()) / 1_000
                            TimeUtils.formatHHMMSS(secondsLeft, Seconds)
                        }

                    }
                }
                Texts.BOSSBAR_STARTING.generate(time)
            },
            progress = {
                val millisLeft = this.startTime - System.currentTimeMillis()
                val percentLeft = millisLeft / (30 * 60 * 1000).toFloat()
                Mth.clamp(1 - percentLeft, 0.0F, 1.0F)
            },
            {
                BossBarColor.YELLOW
            },
            {
                BossBarOverlay.PROGRESS
            }
        )
        this.bossbars.add(start)
        this.reloadBossbars()
    }

    private fun onUHCStart() {
        this.removeBossbars()
        this.setStartTime(Long.MAX_VALUE)

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
                player.sendUHCResourcePack()
            }
            TeamUtils.forEachTeam { team ->
                team.uhc.players.clear()
            }

            val area = this.event.getLobbyHandler().getMap()
            area.removeEntities { it !is Player }
            area.remove()

            this.setPhase(Active)
            val players = PlayerUtils.players().filter { player ->
                val team = player.team
                team !== null && !team.flags.has(Ignored)
            }
            val overworld = LevelUtils.overworld()
            PlayerUtils.spread(overworld, Vec2(0.0F, 0.0F), 500.0, 2900.0, true, players)

            PlayerUtils.forEveryPlayer { player ->
                player.setForUHC(true)
                if (player.isSpectator) {
                    if (player.level() != overworld) {
                        player.teleportTo(overworld, 0.0, 200.0, 0.0, 0.0F, 0.0F)
                    }
                    this.health?.addPlayer(player)
                }
            }
        }
    }

    private fun onUHCActive() {
        this.resetTrackers()
        RuleUtils.setActiveGamerules()

        this.createActiveSidebar()
        this.createActiveBossbar()

        PlayerUtils.forEveryPlayer { player ->
            player.sendSystemMessage(Texts.UHC_GRACE_FIRST.gold())
            player.sendSound(SoundEvents.NOTE_BLOCK_PLING.value())
        }
        this.schedulePhaseTask(10, Minutes, GracePeriodOverTask())

        val graceEnd = this.uptime + Minutes.toTicks(10)
        val grace = ArcadeBossbar(
            title = {
                val remaining = graceEnd - this.uptime
                Texts.BOSSBAR_GRACE.generate(TimeUtils.formatMMSS(remaining, Ticks))
            },
            progress = {
                val percent = (graceEnd - this.uptime) / Minutes.toTicks(10).toFloat()
                BossbarUtils.shrink(0.0F.coerceAtLeast(percent), 0.75F)
            },
            { BossBarColor.GREEN },
            { BossBarOverlay.PROGRESS }
        )
        this.bossbars.add(grace)
        this.reloadBossbars()

        this.schedulePhaseTask(10, Minutes) {
            this.bossbars.remove(grace)
            grace.clearPlayers()
        }

        this.health = ArcadeNameDisplay(Texts.ICON_HEART)
        this.health?.setTitle(Texts.ICON_HEART)
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
            player.abilities.mayfly = true
            player.abilities.invulnerable = true
            player.onUpdateAbilities()
        }

        PlayerUtils.forEveryPlayer { player ->
            if (player.belongsToTeam(team)) {
                player.flags.set(Won, true)
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


        this.schedulePhaseTask(20, Seconds) {
            PlayerUtils.forEveryPlayer {
                it.clearPlayerInventory()
            }
            this.setPhase(Lobby)
        }
    }

    private fun onWorldBorderComplete() {
        UHCMod.logger.info("World Border Completed")
        this.schedulePhaseTask(5, Minutes, BorderEndTask())

        val endTaskTime = this.uptime + Minutes.toTicks(5)
        val endBossbar = ArcadeBossbar(
            title = {
                val remaining = (endTaskTime - this.uptime).coerceAtLeast(0)
                Texts.BOSSBAR_GLOWING.generate(TimeUtils.formatMMSS(remaining, Ticks))
            },
            progress = {
                val percent = (endTaskTime - this.uptime) / Minutes.toTicks(5).toFloat()
                BossbarUtils.shrink(0.0F.coerceAtLeast(percent), 0.75F)
            },
            {
                BossBarColor.GREEN
            },
            {
                BossBarOverlay.PROGRESS
            }
        )
        this.bossbars.add(endBossbar)
        this.reloadBossbars()

        this.schedulePhaseTask(5, Minutes) {
            this.bossbars.remove(endBossbar)
            endBossbar.clearPlayers()
        }
    }

    private fun createActiveSidebar() {
        val sidebar = ArcadeSidebar(ComponentSupplier.of(Texts.CASUAL_UHC.gold().bold()))

        val buffer = "   "

        sidebar.addRow(ComponentSupplier.of(Component.empty().append(buffer).append(Texts.SIDEBAR_TEAMMATES.monospaced())))
        for (i in 0 until this.event.getTeamSize()) {
            sidebar.addRow(TeammateRow(i, buffer))
        }
        sidebar.addRow(ComponentSupplier.of(Component.empty()))
        sidebar.addRow { player ->
            val vectorToBorder = player.distanceToNearestBorder()
            val multiplier = if (vectorToBorder.x < 0 || vectorToBorder.z < 0) -1 else 1
            val distanceToBorder =  multiplier * vectorToBorder.length().toInt()

            val percent = distanceToBorder / (player.level().worldBorder.size / 2.0)
            val colour = if (percent > 0.4) ChatFormatting.DARK_GREEN else if (percent > 0.2) ChatFormatting.YELLOW else if (percent > 0.1) ChatFormatting.RED else ChatFormatting.DARK_RED
            Component.literal(buffer).append(Texts.UHC_DISTANCE_TO_WB.generate(Component.literal(distanceToBorder.toString()).withStyle(colour))).monospaced()
        }
        sidebar.addRow { player ->
            Component.literal(buffer).append(Texts.UHC_WB_RADIUS.generate((player.level().worldBorder.size / 2.0).toInt())).monospaced()
        }
        sidebar.addRow(ComponentSupplier.of(Component.empty()))

        PlayerUtils.forEveryPlayer {
            sidebar.addPlayer(it)
        }
        this.sidebar = sidebar
    }

    private fun createActiveBossbar() {
        val elapsed = ArcadeBossbar(
            UHCBossbarUI(),
            { 1.0F },
            { BossBarColor.YELLOW },
            { BossBarOverlay.PROGRESS }
        )
        this.bossbars.add(elapsed)
        this.reloadBossbars()
    }

    private fun removeBossbars() {
        for (bossbar in this.bossbars) {
            bossbar.clearPlayers()
        }
        this.bossbars.clear()
    }

    private fun reloadBossbars() {
        for (bossbar in this.bossbars) {
            PlayerUtils.forEveryPlayer {
                bossbar.addPlayer(it)
            }
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

    private class UHCBossbarUI: ComponentSupplier {
        override fun getComponent(player: ServerPlayer): Component {
            val start = Component.empty()
                .append(Texts.space(-2))
                .append(Texts.ICON_SHORT_BACKGROUND.shadowless())
                .append(Texts.space(-27))
                .append(Texts.literal("%02d".format(PlayerUtils.players().filter { it.isSurvival }.size)).monospaced())
                .append(Texts.ICON_PLAYERS)
                .append(Texts.space(42))
                .append(Texts.space(1, 2))
            val end = Component.empty()
                .append(Texts.space(39))
                .append(Texts.ICON_SHORT_BACKGROUND.shadowless())
                .append(Texts.space(-27))
                .append(Texts.literal("%02d".format(player.uhcStats[Kills].toInt())).monospaced())
                .append(Texts.ICON_KILLS)
            val middle = Texts.BOSSBAR_ELAPSED.generate(TimeUtils.formatHHMMSS(uptime, Ticks))
            return start.append(middle).append(end)
        }
    }

    private class TeammateRow(val index: Int, val buffer: String = ""): ComponentSupplier {
        override fun getComponent(player: ServerPlayer): Component {
            val team = player.uhc.originalTeam ?: player.team
            if (team == null || team.flags.has(Ignored)) {
                return Component.literal("${this.buffer} - ").monospaced().append(Texts.ICON_CROSS)
            }
            val players = team.uhc.players
            if (this.index >= players.size) {
                return Component.literal("${this.buffer} - ").monospaced().append(Texts.ICON_CROSS)
            }

            val length = (players.maxOfOrNull { it.length } ?: 16)

            val name: String
            val teammate: ServerPlayer?
            if (this.index == 0) {
                name = player.scoreboardName
                teammate = player
            } else {
                name = players.filter { it != player.scoreboardName }[this.index - 1]
                teammate = PlayerUtils.player(name)
            }
            val longName = Component.literal(name + " ".repeat(length - name.length)).withStyle(team.color)
            val start = Component.literal("${this.buffer} - ").append(longName).append(" ").monospaced()
            if (teammate !== null) {
                if (teammate.isSurvival && teammate.isAlive) {
                    val health = "% 4.1f".format(teammate.health / 2.0)
                    return start.append(health).append(Texts.space(1)).append(Texts.ICON_HEART)
                }
                return start.append("     ").append(Texts.ICON_CROSS)
            }
            return start.append("     ").append(Texts.ICON_NO_CONNECTION)
        }
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

            GlobalEventHandler.broadcast(UHCGracePeriodEndEvent())
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
