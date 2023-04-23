package net.casualuhc.uhcmod.managers

import io.netty.buffer.Unpooled
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.events.EventHandler
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.*
import net.casualuhc.arcade.scheduler.GlobalTickedScheduler
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit.Seconds
import net.casualuhc.arcade.utils.ComponentUtils.aqua
import net.casualuhc.arcade.utils.ComponentUtils.bold
import net.casualuhc.arcade.utils.ComponentUtils.gold
import net.casualuhc.arcade.utils.ComponentUtils.lime
import net.casualuhc.arcade.utils.ComponentUtils.red
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.addExtension
import net.casualuhc.arcade.utils.PlayerUtils.clearPlayerInventory
import net.casualuhc.arcade.utils.PlayerUtils.directionToNearestBorder
import net.casualuhc.arcade.utils.PlayerUtils.directionVectorToNearestBorder
import net.casualuhc.arcade.utils.PlayerUtils.distanceToNearestBorder
import net.casualuhc.arcade.utils.PlayerUtils.grantAdvancement
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.arcade.utils.PlayerUtils.message
import net.casualuhc.arcade.utils.PlayerUtils.revokeAdvancement
import net.casualuhc.arcade.utils.PlayerUtils.sendParticles
import net.casualuhc.arcade.utils.PlayerUtils.sendSound
import net.casualuhc.arcade.utils.PlayerUtils.sendSubtitle
import net.casualuhc.arcade.utils.PlayerUtils.sendTitle
import net.casualuhc.arcade.utils.PlayerUtils.teamMessage
import net.casualuhc.arcade.utils.TeamUtils
import net.casualuhc.arcade.utils.TickUtils
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.advancement.RaceAdvancement.Death
import net.casualuhc.uhcmod.advancement.RaceAdvancement.Kill
import net.casualuhc.uhcmod.advancement.UHCAdvancements
import net.casualuhc.uhcmod.events.player.PlayerFlagEvent
import net.casualuhc.uhcmod.events.uhc.UHCSetupEvent
import net.casualuhc.uhcmod.extensions.PlayerFlag.*
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.PlayerStat
import net.casualuhc.uhcmod.extensions.PlayerStat.Deaths
import net.casualuhc.uhcmod.extensions.PlayerStat.Kills
import net.casualuhc.uhcmod.extensions.PlayerStatsExtension
import net.casualuhc.uhcmod.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casualuhc.uhcmod.extensions.PlayerUHCExtension
import net.casualuhc.uhcmod.extensions.PlayerUHCExtension.Companion.uhc
import net.casualuhc.uhcmod.extensions.TeamFlag.Eliminated
import net.casualuhc.uhcmod.extensions.TeamFlag.Ignored
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.TeamUHCExtension.Companion.uhc
import net.casualuhc.uhcmod.managers.TeamManager.hasAlivePlayers
import net.casualuhc.uhcmod.managers.UHCManager.Phase.End
import net.casualuhc.uhcmod.settings.GameSettings
import net.casualuhc.uhcmod.util.Config
import net.casualuhc.uhcmod.util.DirectionUtils.opposite
import net.casualuhc.uhcmod.util.HeadUtils
import net.casualuhc.uhcmod.util.Texts
import net.casualuhc.uhcmod.util.Texts.monospaced
import net.casualuhc.uhcmod.util.Texts.regular
import net.casualuhc.uhcmod.util.shapes.ArrowShape
import net.minecraft.ChatFormatting.*
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffectInstance.INFINITE_DURATION
import net.minecraft.world.effect.MobEffects.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.PlayerHeadItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ClipContext.Block.VISUAL
import net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.HitResult.Type.MISS
import net.minecraft.world.scores.Team
import java.util.*
import kotlin.math.atan2

object PlayerManager {
    private val HEALTH_BOOST = UUID.fromString("a61b8a4f-a4f5-4b7f-b787-d10ba4ad3d57")

    fun ServerPlayer.isMessageGlobal(message: String): Boolean {
        val team = this.team
        return !UHCManager.isActivePhase() || team === null || team.flags.has(Ignored) || message.startsWith('!')
    }

    fun ServerPlayer.dropHead(attacker: Entity? = null) {
        val head = HeadUtils.generateHead(this)
        if (attacker is ServerPlayer) {
            if (attacker.inventory.add(head)) {
                return
            }
        }
        this.drop(head, true, false)
    }

    fun ServerPlayer.resetUHCHealth() {
        val instance = this.attributes.getInstance(Attributes.MAX_HEALTH)
        if (instance != null) {
            instance.removeModifier(HEALTH_BOOST)
            instance.addPermanentModifier(
                AttributeModifier(
                    HEALTH_BOOST,
                    "Health Boost",
                    GameSettings.HEALTH.value,
                    AttributeModifier.Operation.MULTIPLY_BASE
                )
            )
        }
        this.health = this.maxHealth
        this.foodData.setSaturation(20.0F)
    }

    fun ServerPlayer.setForUHC(force: Boolean = true) {
        this.connection.send(ClientboundSetTitleTextPacket(Texts.LOBBY_GOOD_LUCK.copy().gold().bold()))
        this.playNotifySound(SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.MASTER, 1.0F, 1.0F)

        if (force) {
            this.clearPlayerInventory()
            this.resetUHCHealth()
            this.experienceLevel = 0
            this.experienceProgress = 0.0F
        }

        this.removeVehicle()
        this.setGlowingTag(false)

        val flags = this.flags
        flags.set(FullBright, true)

        val team = this.team
        if (team != null && !team.flags.has(Ignored)) {
            team.flags.set(Eliminated, false)

            if (team.players.size == 1) {
                this.grantAdvancement(UHCAdvancements.SOLOIST)
            }

            flags.set(Participating, true)
            flags.set(TeamGlow, true)

            this.uhc.originalTeam = team
            team.uhc.add(this.scoreboardName)

            this.addEffect(MobEffectInstance(DAMAGE_RESISTANCE, 200, 4, true, false))
            this.setGameMode(GameType.SURVIVAL)
            return
        }

        this.setGameMode(GameType.SPECTATOR)
    }

    fun ServerPlayer.updateGlowingTag() {
        this.setGlowingTag(!this.hasGlowingTag())
        this.setGlowingTag(!this.hasGlowingTag())
    }

    fun ServerPlayer.sendUHCResourcePack() {
        this.uhc.hasPack = false
        val info = UHCManager.event.getResourcePackHandler().getInfo(this)
        if (info !== null) {
            this.sendTexturePack(info.url, info.hash, info.isRequired, info.prompt)
        }
    }

    fun ServerPlayer.belongsToTeam(team: Team): Boolean {
        if (this.team === team) {
            return true
        }
        val original = this.uhc.originalTeam
        val players = team.uhc.players
        if (original == team) {
            if (!players.contains(this.scoreboardName)) {
                UHCMod.logger.warn(
                    "Player ${this.scoreboardName} had team ${team.name} registered, but team didn't recognise player??!"
                )
            }
            return true
        } else if (players.contains(this.scoreboardName)) {
            UHCMod.logger.warn(
                "Team ${team.name} had player ${this.scoreboardName} registered, but player didn't recognise team??!"
            )
            return true
        }
        return false
    }

    fun ServerPlayer.giveHeadEffects(stack: ItemStack, hand: InteractionHand) {
        val compound = stack.tag
        var isGolden = false
        if (compound != null) {
            val skullOwner = compound.getCompound(PlayerHeadItem.TAG_SKULL_OWNER)
            if (skullOwner != null) {
                val playerName: String = skullOwner.getString("Name")
                if (playerName == "PhantomTupac") {
                    isGolden = true
                }
            }
        }
        this.addEffect(MobEffectInstance(REGENERATION, if (isGolden) 50 else 60, if (isGolden) 3 else 2))
        this.addEffect(MobEffectInstance(MOVEMENT_SPEED, (if (isGolden) 20 else 15) * 20, 1))
        this.addEffect(MobEffectInstance(SATURATION, 5, 4))

        if (isGolden) {
            this.addEffect(MobEffectInstance(ABSORPTION, 120 * 20, 0))
            this.addEffect(MobEffectInstance(DAMAGE_RESISTANCE, 5 * 20, 0))
        }
        this.swing(hand, true)
        stack.shrink(1)
        this.cooldowns.addCooldown(stack.item, 20)
    }

    @JvmStatic
    fun handleTrackerUpdatePacketForTeamGlowing(
        glowingPlayer: ServerPlayer,
        observingPlayer: ServerPlayer,
        packet: ClientboundSetEntityDataPacket
    ): ClientboundSetEntityDataPacket {
        if (!GameSettings.FRIENDLY_PLAYER_GLOW.value || !UHCManager.isActivePhase()) {
            return packet
        }
        if (!glowingPlayer.isSurvival || !observingPlayer.flags.has(TeamGlow)) {
            return packet
        }
        if (glowingPlayer.team !== observingPlayer.team) {
            return packet
        }

        val tracked = packet.packedItems ?: return packet
        if (tracked.none { it.id == Entity.DATA_SHARED_FLAGS_ID.id }) {
            return packet
        }

        // Make a copy of the packet, because other players are sent the same instance of
        // The packet and may not be on the same team
        val buf = FriendlyByteBuf(Unpooled.buffer())
        packet.write(buf)
        val new = ClientboundSetEntityDataPacket(buf)

        val iterator = new.packedItems.listIterator()
        while (iterator.hasNext()) {
            val value = iterator.next()
            // Need to compare ids because they're not the same instance once re-serialized
            if (value.id() == Entity.DATA_SHARED_FLAGS_ID.id) {
                @Suppress("UNCHECKED_CAST")
                val byteValue = value as SynchedEntityData.DataValue<Byte>
                var flags = byteValue.value()
                flags = (flags.toInt() or (1 shl Entity.FLAG_GLOWING)).toByte()
                iterator.set(SynchedEntityData.DataValue.create(Entity.DATA_SHARED_FLAGS_ID, flags))
            }
        }
        return new
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { this.onPlayerCreated(it) }

        GlobalEventHandler.register<PlayerJoinEvent> { this.onPlayerJoin(it) }
        GlobalEventHandler.register<PlayerPackLoadEvent> { this.onPlayerPack(it) }
        GlobalEventHandler.register<PlayerItemReleaseEvent> { this.onPlayerItemRelease(it) }
        GlobalEventHandler.register<PlayerAttackEvent> { this.onPlayerAttack(it) }
        GlobalEventHandler.register<PlayerDamageEvent> { this.onPlayerDamage(it) }
        GlobalEventHandler.register<PlayerVoidDamageEvent> { this.onPlayerVoidDamage(it) }
        GlobalEventHandler.register<PlayerChatEvent> { this.onPlayerChat(it) }
        GlobalEventHandler.register<PlayerItemUseEvent> { this.onPlayerUseItem(it) }
        GlobalEventHandler.register<PlayerItemUseOnEvent> { this.onPlayerUseItemOn(it) }
        GlobalEventHandler.register<PlayerTickEvent> { this.onPlayerTick(it) }
        GlobalEventHandler.register<PlayerDeathEvent> { this.onPlayerDeath(it) }
        GlobalEventHandler.register<PlayerAdvancementEvent> { this.onPlayerAdvancement(it) }

        GlobalEventHandler.register<UHCSetupEvent> { this.onUHCSetup() }
        GlobalEventHandler.register<PlayerFlagEvent> { this.onPlayerFlag(it) }
    }

    private fun onPlayerCreated(event: PlayerCreatedEvent) {
        val player = event.player
        player.addExtension(PlayerStatsExtension())
        player.addExtension(PlayerFlagsExtension(player))
        player.addExtension(PlayerUHCExtension(player))
    }

    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        player.sendUHCResourcePack()

        player.sendSystemMessage(Texts.LOBBY_WELCOME.append(" Casual UHC").gold())

        val scoreboard = Arcade.server.scoreboard
        if (!UHCManager.hasUHCStarted()) {
            if (!player.hasPermissions(2)) {
                player.setGameMode(GameType.ADVENTURE)
                UHCManager.event.getLobbyHandler().tryTeleport(player)
            } else {
                if (Config.booleanOrDefault("dev", true)) {
                    player.sendSystemMessage(Component.literal("UHC is in dev mode!").red())
                }
            }
        } else {
            if (player.team == null || !player.flags.has(Participating)) {
                player.setGameMode(GameType.SPECTATOR)
            }
        }

        if (player.team == null) {
            val spectator = scoreboard.getPlayerTeam("Spectator")
            if (spectator != null) {
                scoreboard.addPlayerToTeam(player.scoreboardName, spectator)
            }
        }

        // Needed for updating the player's health
        GlobalTickedScheduler.schedule(1, Seconds, player::resetSentInfo)
    }

    private fun onPlayerPack(event: PlayerPackLoadEvent) {
        event.player.uhc.hasPack = true
    }

    private fun onPlayerItemRelease(event: PlayerItemReleaseEvent) {
        if (event.stack.`is`(Items.BOW)) {
            event.player.cooldowns.addCooldown(Items.BOW, (GameSettings.BOW_COOLDOWN.value * 20).toInt())
        }
    }

    private fun onPlayerAttack(event: PlayerAttackEvent) {
        if (UHCManager.isActivePhase() && event.target is ServerPlayer) {
            event.player.uhcStats.increment(PlayerStat.DamageDealt, event.damage.toDouble())
        }
    }

    private fun onPlayerDamage(event: PlayerDamageEvent) {
        if (UHCManager.isActivePhase() && event.player.isSurvival) {
            event.player.uhcStats.increment(PlayerStat.DamageTaken, event.amount.toDouble())
        }
    }

    private fun onPlayerVoidDamage(event: PlayerVoidDamageEvent) {
        if (event.player.isSpectator) {
            event.cancel()
        }
    }

    private fun onPlayerChat(event: PlayerChatEvent) {
        val player = event.player
        val content = event.message.signedContent()
        if (!player.isMessageGlobal(content)) {
            player.teamMessage(event.message)
        } else {
            val decorated = if (content.startsWith('!')) content.substring(1) else content
            player.message(Component.literal(decorated))
        }
        event.cancel()
    }

    private fun onPlayerUseItem(event: PlayerItemUseEvent) {
        if (GameSettings.HEADS_CONSUMABLE.value && event.stack.`is`(Items.PLAYER_HEAD)) {
            event.player.giveHeadEffects(event.stack, event.hand)
            event.cancel(InteractionResultHolder.consume(event.stack))
        }
    }

    private fun onPlayerUseItemOn(event: PlayerItemUseOnEvent) {
        if (GameSettings.HEADS_CONSUMABLE.value && event.stack.`is`(Items.PLAYER_HEAD)) {
            event.player.giveHeadEffects(event.stack, event.context.hand)
            event.cancel(InteractionResult.CONSUME)
        }
    }

    private fun onPlayerTick(event: PlayerTickEvent) {
        this.updateDisplay(event.player)
        if (event.player.isSurvival) {
            this.updateWorldBorder(event.player)
        }
    }

    private fun onPlayerDeath(event: PlayerDeathEvent) {
        event.invoke()
        val player = event.player

        if (UHCManager.isPhase(End)) {
            player.setRespawnPosition(player.level.dimension(), player.blockPosition(), player.xRot, true, false)
            player.setGameMode(GameType.SPECTATOR)
            return
        }
        if (UHCManager.isActivePhase()) {
            player.setRespawnPosition(player.level.dimension(), player.blockPosition(), player.xRot, true, false)
            player.setGameMode(GameType.SPECTATOR)

            if (UHCManager.isUnclaimed(Death)) {
                player.grantAdvancement(UHCAdvancements.EARLY_EXIT)
            }

            val attacker = event.source.entity
            if (attacker is ServerPlayer) {
                attacker.uhcStats.increment(Kills, 1.0)
                if (UHCManager.isUnclaimed(Kill)) {
                    attacker.grantAdvancement(UHCAdvancements.FIRST_BLOOD)
                }
            }

            if (GameSettings.PLAYER_DROPS_GAPPLE_ON_DEATH.value) {
                player.drop(Items.GOLDEN_APPLE.defaultInstance, true, false)
            }

            if (GameSettings.PLAYER_DROPS_HEAD_ON_DEATH.value) {
                player.dropHead(attacker)
            }

            player.uhcStats.increment(Deaths, 1.0)

            val original = player.team
            Arcade.server.scoreboard.addPlayerToTeam(player.scoreboardName, TeamManager.getSpectatorTeam())

            if (original !== null && !original.flags.has(Eliminated) && !original.hasAlivePlayers()) {
                original.flags.set(Eliminated, true)
                PlayerUtils.forEveryPlayer {
                    it.sendSound(SoundEvents.LIGHTNING_BOLT_THUNDER, volume = 0.5F)
                    it.sendSystemMessage(Texts.UHC_ELIMINATED.generate(original.name).withStyle(original.color).bold())
                }
            }

            if (TeamManager.isOneTeamRemaining()) {
                UHCManager.setPhase(End)
            }
        }
    }

    private fun onPlayerAdvancement(event: PlayerAdvancementEvent) {
        event.player.uhcStats.add(event.advancement)
    }

    private fun onUHCSetup() {
        PlayerUtils.forEveryPlayer { player ->
            player.flags.clear()
            player.uhcStats.reset()
            player.uhc.originalTeam = null
            player.uhc.halfHealthTicks = 0
            Arcade.server.advancements.allAdvancements.forEach { advancement ->
                player.revokeAdvancement(advancement)
            }
            player.grantAdvancement(UHCAdvancements.ROOT)
        }
        TeamUtils.forEachTeam { team ->
            team.uhc.players.clear()
        }
    }

    private fun onPlayerFlag(event: PlayerFlagEvent) {
        val player = event.player
        when (event.flag) {
            TeamGlow -> {
                player.updateGlowingTag()
            }
            FullBright -> {
                if (event.value) {
                    player.addEffect(MobEffectInstance(NIGHT_VISION, INFINITE_DURATION, 0, false, false))
                } else {
                    player.removeEffect(NIGHT_VISION)
                }
            }
            else -> { }
        }
    }

    private fun updateDisplay(player: ServerPlayer) {
        if (UHCManager.uptime % 20 == 0 && GameSettings.DISPLAY_TAB.value) {
            val tps = TickUtils.calculateTPS()
            val formatting = if (tps >= 20) DARK_GREEN else if (tps > 15) YELLOW else if (tps > 10) RED else DARK_RED
            val header = Texts.ICON_UHC.append(Texts.space()).append(Texts.CASUAL_UHC.regular().gold().bold()).append(Texts.space()).append(Texts.ICON_UHC)
            val footer = Component.literal("\nServer TPS: ")
                .append(Component.literal("%.1f".format(tps)).withStyle(formatting))
                .append("\n")
                .append(Texts.TAB_HOSTED.aqua().bold())
            player.connection.send(ClientboundTabListPacket(header, footer))
        }
    }

    private fun updateWorldBorder(player: ServerPlayer) {
        val level = player.level
        val border = level.worldBorder

        if (border.isWithinBounds(player.boundingBox)) {
            if (player.flags.has(WasInBorder)) {
                player.flags.set(WasInBorder, false)
                player.connection.send(ClientboundInitializeBorderPacket(border))
            }
            return
        }

        val vector = player.directionVectorToNearestBorder()

        val start = player.eyePosition.add(0.0, 4.0, 0.0)
        val end = start.add(vector.normalize())

        val completed = ArrayList<BlockPos>(10)
        for (i in 1..2) {
            val top = start.lerp(end, 1.5 * i)
            val bottom = top.subtract(0.0, 10.0, 0.0)
            val hit = level.clip(ClipContext(top, bottom, VISUAL, SOURCE_ONLY, player))
            completed.add(hit.blockPos)

            if (hit.type != MISS) {
                val position = hit.blockPos
                val rotation = atan2(vector.x, vector.z)

                val arrow = ArrowShape.createCentred(position.x, hit.location.y + 0.1, position.z, 1.0, rotation)

                for (point in arrow) {
                    player.sendParticles(ParticleTypes.END_ROD, point)
                }
            }
        }

        val direction = player.directionToNearestBorder()
        val fakeDirection = direction.opposite()

        val fakeCenterX = border.centerX + fakeDirection.stepX * border.size
        val fakeCenterZ = border.centerZ + fakeDirection.stepZ * border.size

        val fakeBorder = player.uhc.border
        fakeBorder.setCenter(fakeCenterX, fakeCenterZ)
        fakeBorder.size = border.size + 0.5
        player.connection.send(ClientboundInitializeBorderPacket(fakeBorder))

        if (UHCManager.uptime % 200 == 0) {
            player.sendTitle(Component.empty())
            player.sendSubtitle(Texts.UHC_OUTSIDE_BORDER.generate(Texts.direction(direction).lime()))
        }

        player.flags.set(WasInBorder, true)
    }
}