package net.casual.championships.uhc.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.player.PlayerDamageEvent
import net.casual.arcade.extensions.SerializableExtension
import net.casual.arcade.extensions.event.TeamExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.utils.PlayerUtils.server
import net.casual.arcade.utils.TeamUtils.getOnlinePlayers
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.mixins.PlayerInvoker
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.Difficulty
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodConstants
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.scores.PlayerTeam
import kotlin.math.max
import kotlin.math.min

class TeamSharedHealthExtension(
    private val team: PlayerTeam
): SerializableExtension {
    private var tickTimer = 0
    private var exhaustionLevel = 0.0F
    private var absorption = 0.0F

    var enabled = false

    var maxHealth = 20.0F
    var health = 20.0F
        private set
    var foodLevel = 20
        private set
    var saturationLevel = 5.0F
        private set

    fun teammates(server: MinecraftServer): Collection<ServerPlayer> {
        return this.team.getOnlinePlayers(server)
    }

    fun eat(foodLevelModifier: Int, saturationLevelModifier: Float) {
        this.add(foodLevelModifier, FoodConstants.saturationByModifier(foodLevelModifier, saturationLevelModifier))
    }

    fun eat(properties: FoodProperties) {
        this.add(properties.nutrition, properties.saturation)
    }

    fun hurt(
        server: MinecraftServer,
        source: DamageSource,
        amount: Float,
        excluding: ServerPlayer? = null
    ) {
        this.health -= amount
        for (player in this.team.getOnlinePlayers(server)) {
            if (player != excluding) {
                player.combatTracker.recordDamage(source, amount)
            }
            player.health = this.health
        }
    }

    fun heal(
        server: MinecraftServer,
        amount: Float
    ) {
        if (this.health > 0.0F) {
            this.health += amount

            for (player in this.team.getOnlinePlayers(server)) {
                player.health = this.health
            }
        }
    }

    fun set(server: MinecraftServer, health: Float) {
        this.health = health
        for (player in this.team.getOnlinePlayers(server)) {
            player.health = this.health
        }
    }

    fun setAbsorption(absorption: Float, setter: ServerPlayer) {
        this.absorption = absorption
        for (teammate in this.teammates(setter.server)) {
            if (teammate != setter) {
                (teammate as PlayerInvoker).invokeInternalSetAbsorptionAmount(absorption)
            }
        }
    }

    fun addExhaustion(amount: Float) {
        this.exhaustionLevel = min(this.exhaustionLevel + amount, 40.0F)
    }

    fun hasEnoughFood(): Boolean {
        return this.foodLevel > 6.0F
    }

    fun needsFood(): Boolean {
        return this.foodLevel < 20
    }

    fun tick(server: MinecraftServer) {
        if (!this.enabled) {
            return
        }

        val difficulty = this.getDifficulty()
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = max(this.saturationLevel - 1.0F, 0.0F)
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = max(this.foodLevel - 1, 0)
            }
        }

        val naturalRegen = this.shouldNaturallyRegenerate()
        if (naturalRegen && this.saturationLevel > 0.0F && this.isHurt() && this.foodLevel >= 20) {
            this.tickTimer += 1
            if (this.tickTimer >= 10) {
                val amount = min(this.saturationLevel, 6.0F)
                this.heal(server, amount / 6.0F)
                this.addExhaustion(amount)
                this.tickTimer = 0
            }
        } else if (naturalRegen && this.foodLevel >= 18 && this.isHurt()) {
            this.tickTimer += 1
            if (this.tickTimer >= 80) {
                this.heal(server, 1.0F)
                this.addExhaustion(6.0F)
                this.tickTimer = 0
            }
        } else if (this.foodLevel <= 0) {
            this.tickTimer += 1
            if (this.tickTimer >= 80) {
                if (this.health > 10.0F || difficulty == Difficulty.HARD || this.health > 1.0F && difficulty == Difficulty.NORMAL) {
                    val sources = server.overworld().damageSources()
                    this.hurt(server, sources.starve(), 1.0F)
                }

                this.tickTimer = 0
            }
        } else {
            this.tickTimer = 0
        }

        this.tickDeath(server)
    }

    private fun tickDeath(server: MinecraftServer) {
        if (this.health <= 0.0F) {
            val sources = server.overworld().damageSources()
            for (player in this.team.getOnlinePlayers(server)) {
                player.die(sources.genericKill())
            }
            this.health = this.maxHealth
        }
    }

    override fun id(): Identifier {
        return casual("team_shared_health")
    }

    override fun serialize(output: ValueOutput) {
        output.putBoolean("enabled", this.enabled)
        output.putInt("tick_timer", this.tickTimer)
        output.putFloat("exhaustion_level", this.exhaustionLevel)
        output.putFloat("absorption", this.absorption)
        output.putFloat("max_health", this.maxHealth)
        output.putFloat("health", this.health)
        output.putInt("food_level", this.foodLevel)
        output.putFloat("saturation_level", this.saturationLevel)
    }

    override fun deserialize(input: ValueInput) {
        this.enabled = input.getBooleanOr("enabled", this.enabled)
        this.tickTimer = input.getIntOr("tick_timer", this.tickTimer)
        this.exhaustionLevel = input.getFloatOr("exhaustion_level", this.exhaustionLevel)
        this.absorption = input.getFloatOr("absorption", this.absorption)
        this.maxHealth = input.getFloatOr("max_health", this.maxHealth)
        this.health = input.getFloatOr("health", this.health)
        this.foodLevel = input.getIntOr("food_level", this.foodLevel)
        this.saturationLevel = input.getFloatOr("saturation_level", this.saturationLevel)
    }

    private fun isHurt(): Boolean {
        return this.health > 0.0F && this.health < this.maxHealth
    }

    private fun getDifficulty(): Difficulty {
        // Vanilla gets the difficulty of the dimension that the player
        // is currently in, we'll just assume it's HARD though...
        return Difficulty.HARD
    }

    private fun shouldNaturallyRegenerate(): Boolean {
        // Ditto...
        return false
    }

    private fun add(foodLevel: Int, saturationLevel: Float) {
        this.foodLevel = Mth.clamp(foodLevel + this.foodLevel, 0, 20)
        this.saturationLevel = Mth.clamp(saturationLevel + this.saturationLevel, 0.0f, this.foodLevel.toFloat())
    }

    companion object {
        val PlayerTeam.sharedHealthExtension: TeamSharedHealthExtension
            get() = this.getExtension()

        @JvmStatic
        fun Player.getSharedHealthExtension(): TeamSharedHealthExtension? {
            val team = this.team ?: return null
            val extension = team.sharedHealthExtension
            return if (extension.enabled) extension else null
        }

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<TeamExtensionEvent> { event ->
                event.addExtension(TeamSharedHealthExtension(event.team))
            }
            GlobalEventHandler.Server.register<ServerTickEvent>(::onServerTick)
            GlobalEventHandler.Server.register<PlayerDamageEvent>(phase = PlayerDamageEvent.PHASE_POST, listener = ::onPlayerDamage)
        }

        private fun onServerTick(event: ServerTickEvent) {
            val server = event.server
            for (team in server.scoreboard.playerTeams) {
                team.sharedHealthExtension.tick(server)
            }
        }

        private fun onPlayerDamage(event: PlayerDamageEvent) {
            val (player, source, amount) = event
            val extension = player.getSharedHealthExtension() ?: return
            extension.hurt(player.server, source, amount - extension.absorption, player)
        }
    }
}