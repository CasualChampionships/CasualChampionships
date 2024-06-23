package net.casual.championships.common

import net.casual.arcade.resources.ArcadePacks
import net.casual.arcade.resources.creator.NamedResourcePackCreator
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.ResourcePackUtils.addFont
import net.casual.arcade.utils.ResourcePackUtils.addLangsFrom
import net.casual.arcade.utils.ResourcePackUtils.addLangsFromData
import net.casual.arcade.utils.ResourcePackUtils.addSounds
import net.casual.championships.common.util.*
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object CommonMod: ModInitializer {
    const val MOD_ID = "casual_common"

    private val container = FabricLoader.getInstance().getModContainer(MOD_ID).get()

    val logger: Logger = LoggerFactory.getLogger("CasualCommon")

    val COMMON_PACK = NamedResourcePackCreator.named("common") {
        addAssetSource(MOD_ID)
        addLangsFromData(MOD_ID)
        addLangsFrom("minecraft", container.findPath("data/minecraft/lang").get())
        addFont(CommonComponents.Hud)
        addFont(CommonComponents.Gui)
        addFont(CommonComponents.Text)
        addFont(CommonComponents.Border)
        addSounds(CommonSounds)
        packDescription = "Common resources used in CasualChampionships".literal()
    }

    val COMMON_PACKS = listOf(
        ArcadePacks.NO_SHADOW_PACK,
        ArcadePacks.PADDING_FONT_PACK,
        ArcadePacks.PLAYER_HEADS_PACK,
        ArcadePacks.HIDE_PLAYER_LIST_HEADS_PACK,
        ArcadePacks.MINI_MINECRAFT_FONT,
        ArcadePacks.SPACES_FONT_PACK,
        ArcadePacks.HIDE_PLAYER_LIST_PING_PACK,
        ArcadePacks.ACTION_BAR_FONT_PACK,
        ArcadePacks.MINI_ACTION_BAR_FONT_PACK,
        CommonItems.CUSTOM_MODEL_PACK,
        COMMON_PACK
    )

    override fun onInitialize() {
        CommonItems.noop()
        CommonEntities.noop()
        CommonSounds.noop()

        AntiCheat.registerEvents()
    }

    fun id(path: String): ResourceLocation {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
    }
}