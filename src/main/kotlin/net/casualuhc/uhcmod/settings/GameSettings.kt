package net.casualuhc.uhcmod.settings

import com.google.common.collect.ImmutableMap
import net.casualuhc.arcade.Arcade
import net.casualuhc.arcade.utils.ItemUtils.literalNamed
import net.casualuhc.arcade.utils.ItemUtils.potion
import net.casualuhc.uhcmod.UHCMod
import net.casualuhc.uhcmod.managers.UHCManager
import net.casualuhc.uhcmod.managers.WorldBorderManager
import net.casualuhc.uhcmod.managers.WorldBorderManager.Stage
import net.casualuhc.uhcmod.util.HeadUtils
import net.casualuhc.uhcmod.utils.gamesettings.GameSetting
import net.casualuhc.uhcmod.utils.gamesettings.GameSetting.*
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.Potions
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES
import java.util.function.Consumer

object GameSettings {
    val RULES = LinkedHashMap<ItemStack, GameSetting<*>>()

    @JvmField val WORLD_BORDER_TIME: Int64
    @JvmField val BOW_COOLDOWN: Float64
    @JvmField val HEALTH: Float64
    @JvmField val END_GAME_GLOW: Bool
    @JvmField val FRIENDLY_PLAYER_GLOW: Bool
    @JvmField val PLAYER_DROPS_GAPPLE_ON_DEATH: Bool
    @JvmField val FLOODGATE: Bool
    @JvmField val DISPLAY_TAB: Bool
    @JvmField val PVP: Bool
    @JvmField val OP_POTIONS: Bool
    @JvmField val PLAYER_DROPS_HEAD_ON_DEATH: Bool
    @JvmField val GENERATE_PORTAL: Bool
    @JvmField val MINESWEEPER_ANNOUNCEMENT: Bool
    @JvmField val HEADS_CONSUMABLE: Bool
    @JvmField val SOLO_BUFF: Bool
    @JvmField val WORLD_BORDER_STAGE: Enumerated<Stage>

    init {
        WORLD_BORDER_TIME = register(
            Items.DIAMOND_BOOTS.literalNamed("Border Completion Time"),
            ImmutableMap.of(
                Items.CAKE.literalNamed("10 minutes"), MINUTES.toSeconds(10),
                Items.GREEN_STAINED_GLASS_PANE.literalNamed("2 Hours"), MINUTES.toSeconds(120),
                Items.YELLOW_STAINED_GLASS_PANE.literalNamed("2.5 Hours"), MINUTES.toSeconds(150),
                Items.RED_STAINED_GLASS_PANE.literalNamed("3 Hours"), MINUTES.toSeconds(180)
            ),
            MINUTES.toSeconds(150)
        )
        BOW_COOLDOWN = register(
            Items.BOW.literalNamed("Bow Cooldown"),
            ImmutableMap.of(
                Items.CLOCK.literalNamed("None"), 0.0,
                Items.CLOCK.literalNamed("0.5 Seconds"), 0.5,
                Items.CLOCK.literalNamed("1 Second"), 1.0,
                Items.CLOCK.literalNamed("2 Seconds"), 2.0,
                Items.CLOCK.literalNamed("3 Seconds"), 3.0,
                Items.CLOCK.literalNamed("5 Seconds"), 5.0
            ),
            1.0
        )
        HEALTH = register(
            Items.POTION.literalNamed("Health").potion(Potions.HEALING),
            ImmutableMap.of(
                Items.GREEN_STAINED_GLASS_PANE.literalNamed("Triple"), 2.0,
                Items.YELLOW_STAINED_GLASS_PANE.literalNamed("Double"), 1.0,
                Items.RED_STAINED_GLASS_PANE.literalNamed("Normal"), 0.0
            ),
            1.0
        )
        END_GAME_GLOW = register(
            Items.SPECTRAL_ARROW.literalNamed("End Game Glow"),
            true
        )
        FRIENDLY_PLAYER_GLOW = register(
            Items.GOLDEN_CARROT.literalNamed("Friendly Player Glow"),
            true
        )
        PLAYER_DROPS_GAPPLE_ON_DEATH = register(
            Items.GOLDEN_APPLE.literalNamed("Player Drops Gapple"),
            false
        )
        FLOODGATE = register(
            Items.COBBLESTONE_WALL.literalNamed("Floodgates"),
            false
        )
        DISPLAY_TAB = register(
            Items.WHITE_STAINED_GLASS_PANE.literalNamed("Display Tab Info"),
            true
        )
        PVP = register(
            Items.DIAMOND_SWORD.literalNamed("Pvp"),
            false
        ) { setting -> Arcade.server.isPvpAllowed = setting.value }
        OP_POTIONS = register(
            Items.SPLASH_POTION.literalNamed("Op Potions").potion(Potions.STRONG_HEALING),
            false
        )
        PLAYER_DROPS_HEAD_ON_DEATH = register(
            Items.PLAYER_HEAD.literalNamed("Player Head Drops"),
            true
        )
        GENERATE_PORTAL = register(
            Items.CRYING_OBSIDIAN.literalNamed("Generate Nether Portals"),
            true
        )
        MINESWEEPER_ANNOUNCEMENT = register(
            Items.JUKEBOX.literalNamed("Minesweeper Announcement"),
            true
        )
        HEADS_CONSUMABLE = register(
            HeadUtils.createGoldenHead().setHoverName(Component.literal("Consumable Heads")),
            true
        )
        SOLO_BUFF = register(
            Items.BLACK_BED.literalNamed("Solo Buff"),
            true
        )
        WORLD_BORDER_STAGE = register(
            Items.BARRIER.literalNamed("World Border Stage"),
            Stage.FIRST
        ) { setting ->
            if (UHCManager.isActivePhase()) {
                WorldBorderManager.moveWorldBorders(setting.value.startSize)
                WorldBorderManager.startWorldBorders()
            } else {
                UHCMod.logger.error("Could not set world border since game is not active")
            }
        }
    }

    private fun register(
        name: ItemStack,
        options: Map<ItemStack, Long>,
        default: Long,
        callback: Consumer<GameSetting<Long>>? = null
    ): Int64 {
        return Int64(
            name,
            options,
            default,
            callback
        ).also { RULES[name] = it }
    }

    private fun register(
        name: ItemStack,
        options: Map<ItemStack, Double>,
        default: Double,
        callback: Consumer<GameSetting<Double>>? = null
    ): Float64 {
        return Float64(
            name,
            options,
            default,
            callback
        ).also { RULES[name] = it }
    }

    private fun register(
        name: ItemStack,
        options: Map<ItemStack, Boolean>,
        default: Boolean,
        callback: Consumer<GameSetting<Boolean>>? = null
    ): Bool {
        return Bool(
            name,
            options,
            default,
            callback
        ).also { RULES[name] = it }
    }

    private fun register(
        name: ItemStack,
        default: Boolean,
        callback: Consumer<GameSetting<Boolean>>? = null
    ): Bool {
        return register(
            name,
            ImmutableMap.of(
                Items.GREEN_STAINED_GLASS_PANE.literalNamed("On"), true,
                Items.RED_STAINED_GLASS_PANE.literalNamed("Off"), false
            ),
            default,
            callback
        )
    }

    private inline fun <reified E: Enum<E>> register(
        name: ItemStack,
        default: E,
        callback: Consumer<GameSetting<E>>? = null
    ): Enumerated<E> {
        return Enumerated(
            name,
            getEnumOptions(E::class.java),
            default,
            callback
        ).also { RULES[name] = it }
    }

    private fun <T: Enum<T>?> getEnumOptions(enumClass: Class<T>): Map<ItemStack, T> {
        // This needs to be ordered
        val map: MutableMap<ItemStack, T> = LinkedHashMap<ItemStack, T>()
        var isPurple = true
        for (constant in enumClass.enumConstants) {
            val cleanedName = constant!!.name.lowercase(Locale.getDefault()).split("_").reduce { a, b ->
                a + b.substring(0, 1).uppercase() + b.substring(1)
            }
            val colour: Item = if (isPurple) Items.PURPLE_STAINED_GLASS_PANE else Items.WHITE_STAINED_GLASS_PANE
            map[colour.literalNamed(cleanedName)] = constant
            isPurple = !isPurple
        }
        return map
    }
}
