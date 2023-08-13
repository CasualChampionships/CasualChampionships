package net.casualuhc.uhcmod.advancement

import net.casualuhc.arcade.advancements.AdvancementBuilder
import net.casualuhc.arcade.events.GlobalEventHandler
import net.casualuhc.arcade.events.player.*
import net.casualuhc.arcade.events.server.ServerAdvancementReloadEvent
import net.casualuhc.arcade.scheduler.GlobalTickedScheduler
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit
import net.casualuhc.arcade.utils.ItemUtils.potion
import net.casualuhc.arcade.utils.PlayerUtils
import net.casualuhc.arcade.utils.PlayerUtils.getExtension
import net.casualuhc.arcade.utils.PlayerUtils.grantAdvancement
import net.casualuhc.arcade.utils.PlayerUtils.isSurvival
import net.casualuhc.uhcmod.advancement.RaceAdvancement.Craft
import net.casualuhc.uhcmod.events.uhc.UHCEndEvent
import net.casualuhc.uhcmod.extensions.PlayerFlag.Participating
import net.casualuhc.uhcmod.extensions.PlayerFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.extensions.PlayerStat.DamageDealt
import net.casualuhc.uhcmod.extensions.PlayerStat.Relogs
import net.casualuhc.uhcmod.extensions.PlayerStatsExtension.Companion.uhcStats
import net.casualuhc.uhcmod.extensions.PlayerUHCExtension
import net.casualuhc.uhcmod.extensions.TeamFlag.Eliminated
import net.casualuhc.uhcmod.extensions.TeamFlagsExtension.Companion.flags
import net.casualuhc.uhcmod.managers.PlayerManager.isMessageGlobal
import net.casualuhc.uhcmod.managers.UHCManager
import net.casualuhc.uhcmod.screen.MinesweeperScreen
import net.casualuhc.uhcmod.util.ResourceUtils
import net.casualuhc.uhcmod.util.ResourceUtils.id
import net.casualuhc.uhcmod.util.Texts
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.FrameType
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FallingBlock

object UHCAdvancements {
    private val ALL = HashSet<Advancement>()

    val ROOT: Advancement
    val FIRST_BLOOD: Advancement
    val EARLY_EXIT: Advancement
    val MOSTLY_HARMLESS: Advancement
    val HEAVY_HITTER: Advancement
    val WINNER: Advancement
    val COMBAT_LOGGER: Advancement
    val ON_THE_EDGE: Advancement
    val NOT_DUSTLESS: Advancement
    val PARKOUR_MASTER: Advancement
    val WORLD_RECORD_PACE: Advancement
    val EMBARRASSING: Advancement
    val BUSTED: Advancement
    val DEMOLITION_EXPERT: Advancement
    val OK_WE_BELIEVE_YOU_NOW: Advancement
    val FALLING_BLOCK: Advancement
    val DREAM_LUCK: Advancement
    val BROKEN_ANKLES: Advancement
    val SKILL_ISSUE: Advancement
    val SOLOIST: Advancement
    val LDAP: Advancement
    val NOT_NOW: Advancement
    val OFFICIALLY_BORED: Advancement
    val FIND_THE_BUTTON: Advancement
    val DISTRACTED: Advancement
    val UH_OH: Advancement
    val BASICALLY: Advancement
    val TEAM_PLAYER: Advancement
    val LAST_MAN_STANDING: Advancement

    init {
        ROOT = AdvancementBuilder.create {
            id = id("root")
            display(Items.GOLDEN_APPLE)
            title = Texts.ADVANCEMENT_ROOT
            description = Texts.ADVANCEMENT_ROOT_DESC
            background = ResourceLocation("textures/gui/advancements/backgrounds/adventure.png")
            impossible()
        }.build()
        FIRST_BLOOD = AdvancementBuilder.create {
            parent = ROOT
            id = id("first_blood")
            display(Items.IRON_SWORD)
            title = Texts.ADVANCEMENT_FIRST_BLOOD
            description = Texts.ADVANCEMENT_FIRST_BLOOD_DESC
            toast()
            announce()
            impossible()
        }.build()
        EARLY_EXIT = AdvancementBuilder.create {
            parent = ROOT
            id = id("early_exit")
            display(Items.POISONOUS_POTATO)
            title = Texts.ADVANCEMENT_EARLY_EXIT
            description = Texts.ADVANCEMENT_EARLY_EXIT_DESC
            toast()
            announce()
            impossible()
        }.build()
        MOSTLY_HARMLESS = AdvancementBuilder.create {
            parent = EARLY_EXIT
            id = id("mostly_harmless")
            display(Items.FEATHER)
            title = Texts.ADVANCEMENT_MOSTLY_HARMLESS
            description = Texts.ADVANCEMENT_MOSTLY_HARMLESS_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }.build()
        HEAVY_HITTER = AdvancementBuilder.create {
            parent = FIRST_BLOOD
            id = id("heavy_hitter")
            display(Items.ANVIL)
            title = Texts.ADVANCEMENT_HEAVY_HITTER
            description = Texts.ADVANCEMENT_HEAVY_HITTER_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }.build()
        WINNER = AdvancementBuilder.create {
            parent = FIRST_BLOOD
            id = id("winner")
            display(Items.TOTEM_OF_UNDYING)
            title = Texts.ADVANCEMENT_WINNER
            description = Texts.ADVANCEMENT_WINNER_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }.build()
        COMBAT_LOGGER = AdvancementBuilder.create {
            parent = ROOT
            id = id("combat_logger")
            display(Items.WOODEN_SWORD)
            title = Texts.ADVANCEMENT_COMBAT_LOGGER
            description = Texts.ADVANCEMENT_COMBAT_LOGGER_DESC
            toast()
            announce()
            impossible()
        }.build()
        NOT_DUSTLESS = AdvancementBuilder.create {
            parent = ROOT
            id = id("thats_not_dustless")
            display(Items.REDSTONE)
            title = Texts.ADVANCEMENT_NOT_DUSTLESS
            description = Texts.ADVANCEMENT_NOT_DUSTLESS_DESC
            toast()
            announce()
            impossible()
        }.build()
        PARKOUR_MASTER = AdvancementBuilder.create {
            parent = ROOT
            id = id("parkour_master")
            display(Items.NETHERITE_BOOTS)
            title = Texts.ADVANCEMENT_PARKOUR_MASTER
            description = Texts.ADVANCEMENT_PARKOUR_MASTER_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }.build()
        WORLD_RECORD_PACE = AdvancementBuilder.create {
            parent = ROOT
            id = id("world_record_pace")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.SWIFTNESS)
            title = Texts.ADVANCEMENT_WORLD_RECORD_PACE
            description = Texts.ADVANCEMENT_WORLD_RECORD_PACE_DESC
            toast()
            announce()
            impossible()
        }.build()
        EMBARRASSING = AdvancementBuilder.create {
            parent = ROOT
            id = id("thats_embarrassing")
            display(Items.SWEET_BERRIES)
            title = Texts.ADVANCEMENT_EMBARRASSING
            description = Texts.ADVANCEMENT_EMBARRASSING_DESC
            toast()
            announce()
            impossible()
        }.build()
        BUSTED = AdvancementBuilder.create {
            parent = ROOT
            id = id("busted")
            display(Items.BARRIER)
            title = Texts.ADVANCEMENT_BUSTED
            description = Texts.ADVANCEMENT_BUSTED_DESC
            toast()
            announce()
            impossible()
        }.build()
        DEMOLITION_EXPERT = AdvancementBuilder.create {
            parent = EARLY_EXIT
            id = id("demolition_expert")
            display(Items.TNT)
            title = Texts.ADVANCEMENT_DEMOLITION_EXPERT
            description = Texts.ADVANCEMENT_DEMOLITION_EXPERT_DESC
            toast()
            announce()
            impossible()
        }.build()
        OK_WE_BELIEVE_YOU_NOW = AdvancementBuilder.create {
            parent = COMBAT_LOGGER
            id = id("ok_we_believe_you_now")
            display(Items.WOODEN_HOE)
            title = Texts.ADVANCEMENT_WE_BELIEVE
            description = Texts.ADVANCEMENT_WE_BELIEVE_DESC
            toast()
            announce()
            impossible()
        }.build()
        FALLING_BLOCK = AdvancementBuilder.create {
            parent = NOT_DUSTLESS
            id = id("falling_block")
            display(Items.SAND)
            title = Texts.ADVANCEMENT_FALLING_BLOCK
            description = Texts.ADVANCEMENT_FALLING_BLOCK_DESC
            toast()
            announce()
            impossible()
        }.build()
        DREAM_LUCK = AdvancementBuilder.create {
            parent = BUSTED
            id = id("dream_luck")
            display(Items.ENCHANTED_GOLDEN_APPLE)
            title = Texts.ADVANCEMENT_DREAM_LUCK
            description = Texts.ADVANCEMENT_DREAM_LUCK_DESC
            toast()
            announce()
            impossible()
        }.build()
        BROKEN_ANKLES = AdvancementBuilder.create {
            parent = EMBARRASSING
            id = id("broken_ankles")
            display(Items.LEATHER_BOOTS)
            title = Texts.ADVANCEMENT_BROKEN_ANKLES
            description = Texts.ADVANCEMENT_BROKEN_ANKLES_DESC
            toast()
            announce()
            impossible()
        }.build()
        ON_THE_EDGE = AdvancementBuilder.create {
            parent = BROKEN_ANKLES
            id = id("on_the_edge")
            display = Items.SPLASH_POTION.defaultInstance.potion(Potions.STRONG_HARMING)
            title = Texts.ADVANCEMENT_ON_THE_EDGE
            description = Texts.ADVANCEMENT_ON_THE_EDGE_DESC
            toast()
            announce()
            impossible()
        }.build()
        SKILL_ISSUE = AdvancementBuilder.create {
            parent = EARLY_EXIT
            id = id("skill_issue")
            display(Items.BONE)
            title = Texts.ADVANCEMENT_SKILL_ISSUE
            description = Texts.ADVANCEMENT_SKILL_ISSUE_DESC
            toast()
            announce()
            impossible()
        }.build()
        SOLOIST = AdvancementBuilder.create {
            parent = ROOT
            id = id("soloist")
            display(Items.PLAYER_HEAD)
            title = Texts.ADVANCEMENT_SOLOIST
            description = Texts.ADVANCEMENT_SOLOIST_DESC
            toast()
            announce()
            impossible()
        }.build()
        NOT_NOW = AdvancementBuilder.create {
            parent = ROOT
            id = id("not_now")
            display(Items.NETHERITE_SWORD)
            title = Texts.ADVANCEMENT_NOT_NOW
            description = Texts.ADVANCEMENT_NOT_NOW_DESC
            toast()
            announce()
            impossible()
        }.build()
        LDAP = AdvancementBuilder.create {
            parent = NOT_NOW
            id = id("ldap")
            display(Items.EMERALD_BLOCK)
            title = Texts.ADVANCEMENT_KATIE
            description = Texts.ADVANCEMENT_KATIE_DESC
            toast()
            announce()
            impossible()
        }.build()
        OFFICIALLY_BORED = AdvancementBuilder.create {
            parent = WORLD_RECORD_PACE
            id = id("officially_bored")
            display(Items.COMMAND_BLOCK)
            title = Texts.ADVANCEMENT_BORED
            description = Texts.ADVANCEMENT_BORED_DESC
            toast()
            announce()
            impossible()
        }.build()
        DISTRACTED = AdvancementBuilder.create {
            parent = OFFICIALLY_BORED
            id = id("distracted")
            display(Items.CHAIN_COMMAND_BLOCK)
            title = Texts.ADVANCEMENT_DISTRACTED
            description = Texts.ADVANCEMENT_DISTRACTED_DESC
            toast()
            announce()
            impossible()
        }.build()
        UH_OH = AdvancementBuilder.create {
            parent = LDAP
            id = id("uh_oh")
            display(Items.BARRIER)
            title = Texts.ADVANCEMENT_UH_OH
            description = Texts.ADVANCEMENT_UH_OH_DESC
            toast()
            announce()
            impossible()
        }.build()
        BASICALLY = AdvancementBuilder.create {
            parent = LDAP
            id = id("basically")
            display(Items.WHITE_WOOL)
            title = Texts.ADVANCEMENT_BASICALLY
            description = Texts.ADVANCEMENT_BASICALLY_DESC
            toast()
            announce()
            impossible()
        }.build()
        TEAM_PLAYER = AdvancementBuilder.create {
            parent = SOLOIST
            id = id("team_player")
            display(Items.LIME_BANNER)
            title = Texts.ADVANCEMENT_TEAM_PLAYER
            description = Texts.ADVANCEMENT_TEAM_PLAYER_DESC
            toast()
            announce()
            impossible()
        }.build()
        FIND_THE_BUTTON = AdvancementBuilder.create {
            parent = OFFICIALLY_BORED
            id = id("find_the_button")
            display(Items.STONE_BUTTON)
            title = Texts.ADVANCEMENT_FIND_THE_BUTTON
            description = Texts.ADVANCEMENT_FIND_THE_BUTTON_DESC
            frame = FrameType.CHALLENGE
            toast()
            announce()
            impossible()
        }.build()
        LAST_MAN_STANDING = AdvancementBuilder.create {
            parent = SOLOIST
            id = id("soloist")
            display(Items.ARROW)
            title = Texts.ADVANCEMENT_LAST_MAN
            description = Texts.ADVANCEMENT_LAST_MAN_DESC
            toast()
            announce()
            impossible()
        }.build()
    }

    fun register(advancement: Advancement) {
        ALL.add(advancement)
    }

    fun isRegistered(advancement: Advancement): Boolean {
        return ALL.contains(advancement)
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerAdvancementReloadEvent>(0) {
            ALL.clear()
            for (field in UHCAdvancements::class.java.declaredFields) {
                if (Advancement::class.java.isAssignableFrom(field.type)) {
                    try {
                        val advancement = field[null] as Advancement
                        it.add(advancement)
                        this.register(advancement)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        GlobalEventHandler.register<UHCEndEvent> {
            var lowest: PlayerAttacker? = null
            var highest: PlayerAttacker? = null
            for (player in PlayerUtils.players()) {
                if (player.flags.has(Participating)) {
                    val current = player.uhcStats[DamageDealt]
                    if (lowest === null) {
                        val first = PlayerAttacker(player, current)
                        lowest = first
                        highest = first
                    }
                    if (lowest.damageDealt > current) {
                        lowest = PlayerAttacker(player, current)
                    } else if (highest!!.damageDealt < current) {
                        highest = PlayerAttacker(player, current)
                    }
                }
            }
            if (lowest != null) {
                lowest.player.grantAdvancement(MOSTLY_HARMLESS)
                highest!!.player.grantAdvancement(HEAVY_HITTER)
            }
        }
        GlobalEventHandler.register<PlayerJoinEvent> { event ->
            if (UHCManager.isActivePhase() && event.player.isSurvival) {
                val stats = event.player.uhcStats
                stats.increment(Relogs, 1.0)

                // Wait for player to load in
                GlobalTickedScheduler.schedule(5, MinecraftTimeUnit.Seconds) {
                    event.player.grantAdvancement(COMBAT_LOGGER)
                    if (stats[Relogs] == 10.0) {
                        event.player.grantAdvancement(OK_WE_BELIEVE_YOU_NOW)
                    }
                }

                val team = event.player.team
                if (team !== null && team.flags.has(Eliminated)) {
                    team.flags.set(Eliminated, false)
                    event.player.grantAdvancement(TEAM_PLAYER)
                }
            }
        }
        GlobalEventHandler.register<PlayerDeathEvent> { event ->
            if (event.player.containerMenu is MinesweeperScreen) {
                event.player.grantAdvancement(DISTRACTED)
            }
        }
        GlobalEventHandler.register<PlayerBlockPlacedEvent> { event ->
            val state = event.state
            val block = state.block
            val context = event.context
            val pos = context.clickedPos
            val world = context.level
            if (block is FallingBlock && FallingBlock.isFree(world.getBlockState(pos.below())) || pos.y < world.minBuildHeight) {
                event.player.grantAdvancement(FALLING_BLOCK)
            } else if (block === Blocks.REDSTONE_WIRE) {
                event.player.grantAdvancement(NOT_DUSTLESS)
            } else if (block === Blocks.TNT) {
                event.player.grantAdvancement(DEMOLITION_EXPERT)
            }
        }
        GlobalEventHandler.register<PlayerCraftEvent> { event ->
            if (event.stack.`is`(Items.CRAFTING_TABLE) && UHCManager.isUnclaimed(Craft)) {
                event.player.grantAdvancement(WORLD_RECORD_PACE)
            }
        }
        GlobalEventHandler.register<PlayerBorderDamageEvent> { event ->
            if (event.invoke() && event.player.isDeadOrDying) {
                event.player.grantAdvancement(SKILL_ISSUE)
            }
        }
        GlobalEventHandler.register<PlayerLootEvent> { event ->
            if (event.items.any { it.`is`(Items.ENCHANTED_GOLDEN_APPLE) }) {
                event.player.grantAdvancement(DREAM_LUCK)
            }
        }
        GlobalEventHandler.register<PlayerTickEvent> { event ->
            val player = event.player
            val extension = event.player.getExtension(PlayerUHCExtension::class.java)
            if (player.isSurvival && player.flags.has(Participating) && player.health <= 1.0) {
                if (++extension.halfHealthTicks == 1200) {
                    player.grantAdvancement(ON_THE_EDGE)
                }
            } else {
                extension.halfHealthTicks = 0
            }
        }
        GlobalEventHandler.register<PlayerLandEvent> { event ->
            if (UHCManager.isActivePhase() && UHCManager.uptime < 1200 && event.damage > 0) {
                event.player.grantAdvancement(BROKEN_ANKLES)
            }
        }
        GlobalEventHandler.register<PlayerFallEvent> { event ->
            if (UHCManager.isLobbyPhase() && !event.player.hasPermissions(4)) {
                if (UHCManager.event.getLobbyHandler().tryTeleport(event.player)) {
                    event.player.grantAdvancement(UH_OH)
                }
            }
        }
        GlobalEventHandler.register<PlayerChatEvent> { event ->
            val message: String = event.message.signedContent().lowercase()
            if (event.player.isMessageGlobal(message)) {
                if (message.contains("jndi") && message.contains("ldap")) {
                    event.player.grantAdvancement(LDAP)
                }
                if (message.contains("basically")) {
                    event.player.grantAdvancement(BASICALLY)
                }
            }
        }
        GlobalEventHandler.register<PlayerBlockCollisionEvent> { event ->
            if (event.state.`is`(Blocks.SWEET_BERRY_BUSH)) {
                event.entity.grantAdvancement(EMBARRASSING)
            }
        }
        GlobalEventHandler.register<PlayerAdvancementEvent> { event ->
            event.announce = this.isRegistered(event.advancement) && event.announce
        }
    }

    private class PlayerAttacker(
        val player: ServerPlayer,
        val damageDealt: Double
    )
}
