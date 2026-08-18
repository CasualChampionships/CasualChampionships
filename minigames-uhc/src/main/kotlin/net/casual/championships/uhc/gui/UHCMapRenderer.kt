package net.casual.championships.uhc.gui

import eu.pb4.mapcanvas.api.core.*
import it.unimi.dsi.fastutil.doubles.Double2ObjectFunction
import it.unimi.dsi.fastutil.doubles.Double2ObjectLinkedOpenHashMap
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.boundary.utils.levelBoundary
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevel
import net.casual.arcade.pack.font.heads.ProfileHeadComponents
import net.casual.arcade.pack.font.heads.getHeadOrDefaultFor
import net.casual.arcade.pack.font.spacing.SpacingFontResources
import net.casual.arcade.pack.utils.withMiniFont
import net.casual.arcade.utils.ItemUtils.hideTooltip
import net.casual.arcade.utils.ItemUtils.named
import net.casual.arcade.utils.component.yellow
import net.casual.arcade.utils.registries.isOf
import net.casual.arcade.utils.scoreboard.color
import net.casual.championships.common.util.casual
import net.casual.championships.uhc.minigame.UHCMinigame
import net.casual.championships.uhc.utils.UHCComponents
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BiomeTags
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class UHCMapRenderer(private val uhc: UHCMinigame) {
    private val canvases = LinkedHashMap<ResourceKey<Level>, CanvasData>()
    private val maps = Object2ObjectOpenHashMap<ResourceKey<Level>, Double2ObjectMap<CanvasImage>>(3)

    fun startWatching(player: ServerPlayer) {
        for (data in this.canvases.values) {
            data.canvas.addPlayer(player)
        }
    }

    fun stopWatching(player: ServerPlayer) {
        for (data in this.canvases.values) {
            data.canvas.removePlayer(player)
        }
    }

    fun getMaps(): List<ItemStack> {
        return this.canvases.values.map { data ->
            val map = data.canvas.asStack().named(data.dimensionIcon.name!!)
            val model = data.model
            map.hideTooltip(DataComponents.MAP_ID)
            if (model != null) {
                map.set(DataComponents.ITEM_MODEL, model)
            }
            map
        }
    }

    fun clear() {
        this.maps.clear()
    }

    fun update(level: ServerLevel) {
        val dimension = VanillaLikeLevel.getLikeDimension(level)
        val (canvas, _, _, sizeIcon, playerIcons) = this.canvases.getOrPut(level.dimension()) {
            val (dimensionName, model) = when (dimension) {
                Level.OVERWORLD -> "overworld" to OVERWORLD_ID
                Level.NETHER -> "nether" to NETHER_ID
                Level.END -> "end" to END_ID
                else -> level.dimension().identifier().path to null
            }

            val canvas = DrawableCanvas.create()
            val formattedDimension = Component.literal(dimensionName).withMiniFont().yellow()
            CanvasData(
                canvas,
                model,
                canvas.createIcon(MapDecorationTypes.TARGET_X, true, 26, 220, 0, formattedDimension),
                canvas.createIcon(MapDecorationTypes.TARGET_X, true, 26, 236, 0, null),
                Object2ObjectOpenHashMap()
            )
        }

        // Let's just assume that our center is stationary because that makes things easier.
        // Let's also assume that our boundary is square because that also makes things easier
        val phase = this.uhc.boundary.phase
        var startSize = this.uhc.boundary.getSizeAndCenter(level, phase.getStart(level)).size.x
        val endSize = this.uhc.boundary.getSizeAndCenter(level, phase.getEnd(level)).size.x

        val boundary = level.levelBoundary ?: return
        if (boundary.getSize().x == endSize) {
            startSize = endSize
        }

        startSize = max(startSize * 1.1, 32.0)

        val (edge, outer) = if (boundary.getStatus() == BoundaryShape.Status.Stationary) {
            CanvasColor.LAPIS_BLUE_HIGH to CanvasColor.LAPIS_BLUE_NORMAL
        } else {
            CanvasColor.DULL_RED_HIGH to CanvasColor.DULL_RED_NORMAL
        }

        val box = boundary.getAABB()
        val center = boundary.getCenter()
        val borderMinX = box.minX
        val borderMinZ = box.minZ
        val borderMaxX = box.maxX
        val borderMaxZ = box.maxZ

        val scale = startSize / 128
        val scaledBorderMinX = Mth.floor(borderMinX / scale + 0.5) + 64
        val scaledBorderMinZ = Mth.floor(borderMinZ / scale + 0.5) + 64
        val scaledBorderMaxX = Mth.floor(borderMaxX / scale - 0.5) + 64
        val scaledBorderMaxZ = Mth.floor(borderMaxZ / scale - 0.5) + 64

        val map = this.getOrCreateMap(level, dimension, center.x, center.z, startSize)
        for (x in 0..< 128) {
            for (z in 0..< 128) {
                val isInCenter = x in (scaledBorderMinX)..scaledBorderMaxX
                    && z in (scaledBorderMinZ..scaledBorderMaxZ)
                if (!isInCenter) {
                    canvas.set(x, z, outer)
                    continue
                }
                if (x == scaledBorderMinX || x == scaledBorderMaxX) {
                    canvas.set(x, z, edge)
                    continue
                }
                if (z == scaledBorderMinZ || z == scaledBorderMaxZ) {
                    canvas.set(x, z, edge)
                    continue
                }
                canvas.setRaw(x, z, map.getRaw(x, z))
            }
        }

        val roundedStartSize = startSize.roundToInt()
        sizeIcon.name = Component.literal("$roundedStartSize x $roundedStartSize").withMiniFont().yellow()

        for (players in level.players()) {
            if (this.isPlayerValidForIcon(players, level, center.x, center.z, startSize)) {
                playerIcons.computeIfAbsent(players.uuid) {
                    canvas.createIcon(MapDecorationTypes.TARGET_X, true, 0, 0, 0, null)
                }
            }
        }

        val playerScale = startSize / 256
        val players = this.uhc.server.playerList
        val iter = playerIcons.iterator()
        for ((uuid, icon) in iter) {
            val player = players.getPlayer(uuid)
            if (player == null || !this.isPlayerValidForIcon(player, level, center.x, center.z, startSize)) {
                canvas.removeIcon(icon)
                iter.remove()
                continue
            }
            this.updatePlayerIcon(icon, player, playerScale)
        }

        canvas.sendUpdates()
    }

    private fun isPlayerValidForIcon(
        player: ServerPlayer,
        level: ServerLevel,
        centerX: Double,
        centerZ: Double,
        size: Double
    ): Boolean {
        if (player.level().dimension() != level.dimension()) {
            return false
        }
        if (this.uhc.players.isSpectating(player)) {
            return false
        }
        return (abs(player.x - centerX) < size / 2) && (abs(player.z - centerZ) < size / 2)
    }

    private fun updatePlayerIcon(icon: CanvasIcon, player: ServerPlayer, playerScale: Double) {
        val scaledPlayerX = (player.x / playerScale).toInt() + 128
        val scaledPlayerZ = (player.z / playerScale).toInt() + 116

        icon.move(scaledPlayerX, scaledPlayerZ, 0)
        // if (icon.text == null) {
        val head = ProfileHeadComponents.getHeadOrDefaultFor(player)
        icon.name = Component.empty()
            .append(head)
            .append(SpacingFontResources.spaced(-10))
            .append(UHCComponents.Bitmap.PLAYER_BACKGROUND.copy().color(player.team))
            .append(SpacingFontResources.spaced(-1))
        // }
    }

    private fun getOrCreateMap(
        level: ServerLevel,
        dimension: ResourceKey<Level>,
        centerX: Double,
        centerZ: Double,
        size: Double
    ): CanvasImage {
        val canvases = this.maps.getOrPut(level.dimension()) { Double2ObjectLinkedOpenHashMap() }
        return canvases.computeIfAbsent(size, Double2ObjectFunction {
            val corner = BlockPos.containing(
                centerX - size / 2,
                level.seaLevel.toDouble() + 20,
                centerZ - size / 2
            )
            if (size > 128 || dimension == Level.NETHER) {
                this.createBiomeMap(level, corner, size / 128)
            } else {
                this.createBlockMap(level, corner, size / 128)
            }
        })
    }

    private fun createBlockMap(
        level: ServerLevel,
        from: BlockPos,
        step: Double
    ): CanvasImage {
        val canvas = CanvasImage(128, 128)
        val pos = from.mutable()
        var dx = 0.0
        var dy = 0.0
        for (x in 0..< 128) {
            for (y in 0..< 128) {
                val chunk = level.getChunk(pos)
                var height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE,pos.x and 15, pos.z and 15)
                var state: BlockState
                do {
                    pos.y = --height
                    state = chunk.getBlockState(pos)
                } while (state.getMapColor(level, pos) == MapColor.NONE && height > level.minY)

                canvas.set(x, y, state.getMapColor(level, pos), MapColor.Brightness.NORMAL)
                dy += step
                pos.z = from.z + Mth.floor(dy)
            }
            dx += step
            dy = 0.0
            pos.x = from.x + Mth.floor(dx)
            pos.z = from.z
        }
        return canvas
    }

    private fun createBiomeMap(
        level: ServerLevel,
        from: BlockPos,
        step: Double
    ): CanvasImage {
        val canvas = CanvasImage(128, 128)
        val pos = from.mutable()
        var dx = 0.0
        var dy = 0.0
        for (x in 0..< 128) {
            for (y in 0..< 128) {
                canvas.set(x, y, this.biomeToCanvasColor(level.getBiome(pos), pos))
                dy += step
                pos.z = from.z + Mth.floor(dy)
            }
            dx += step
            dy = 0.0
            pos.x = from.x + Mth.floor(dx)
            pos.z = from.z
        }
        return canvas
    }

    private fun biomeToCanvasColor(biome: Holder<Biome>, pos: BlockPos): CanvasColor {
        if (biome.isOf(BiomeTags.IS_OCEAN) || biome.isOf(BiomeTags.IS_DEEP_OCEAN) || biome.isOf(BiomeTags.IS_RIVER)) {
            return CanvasColor.WATER_BLUE_NORMAL
        }
        if (biome.isOf(BiomeTags.IS_BADLANDS)){
            return CanvasColor.TERRACOTTA_ORANGE_HIGH
        }
        if (biome.isOf(Biomes.DESERT) || biome.isOf(BiomeTags.IS_BEACH)) {
            return CanvasColor.PALE_YELLOW_NORMAL
        }
        if (biome.isOf(BiomeTags.IS_MOUNTAIN)){
            return CanvasColor.STONE_GRAY_NORMAL
        }
        if (biome.isOf(BiomeTags.IS_SAVANNA)){
            return CanvasColor.GREEN_NORMAL
        }
        if (biome.isOf(BiomeTags.IS_JUNGLE)) {
            return CanvasColor.EMERALD_GREEN_LOW
        }
        if (biome.isOf(Biomes.SNOWY_PLAINS) || biome.isOf(Biomes.SNOWY_TAIGA)) {
            return CanvasColor.WHITE_NORMAL
        }
        if (biome.isOf(Biomes.NETHER_WASTES)) {
            return CanvasColor.DARK_RED_NORMAL
        }
        if (biome.isOf(Biomes.WARPED_FOREST)) {
            return CanvasColor.BRIGHT_TEAL_NORMAL
        }
        if (biome.isOf(Biomes.CRIMSON_FOREST)) {
            return CanvasColor.RED_NORMAL
        }
        if (biome.isOf(Biomes.BASALT_DELTAS)) {
            return CanvasColor.DEEPSLATE_GRAY_NORMAL
        }
        if (biome.isOf(Biomes.SOUL_SAND_VALLEY)) {
            return CanvasColor.BROWN_NORMAL
        }
        if (biome.isOf(Biomes.END_HIGHLANDS) || biome.isOf(Biomes.END_MIDLANDS) || biome.isOf(Biomes.END_BARRENS)) {
            return CanvasColor.PALE_YELLOW_LOW
        }
        if (biome.isOf(Biomes.THE_VOID) || biome.isOf(Biomes.SMALL_END_ISLANDS)) {
            return CanvasColor.BLACK_NORMAL
        }
        if (biome.isOf(Biomes.THE_END)) {
            if (pos.x * pos.x + pos.z * pos.z < 50 * 50) {
                return CanvasColor.PALE_YELLOW_LOW
            }
            return CanvasColor.BLACK_NORMAL
        }
        return CanvasColor.PALE_GREEN_NORMAL
    }

    private data class CanvasData(
        val canvas: PlayerCanvas,
        val model: Identifier?,
        val dimensionIcon: CanvasIcon,
        val sizeIcon: CanvasIcon,
        val playerIcons: MutableMap<UUID, CanvasIcon>
    )

    companion object {
        private val OVERWORLD_ID = casual("gui/overworld_map")
        private val NETHER_ID = casual("gui/nether_map")
        private val END_ID = casual("gui/end_map")

        fun load() {

        }
    }
}