package net.casual.championships.common.items

import net.casual.arcade.utils.registries.NamespacedItemRegistryRegister
import net.casual.championships.common.items.gui.DummyItem
import net.casual.championships.common.items.gui.ForwardFacingPlayerHead
import net.casual.championships.common.items.gui.TintableDummyItem
import net.casual.championships.common.items.minigame.GoldenHeadItem
import net.casual.championships.common.items.minigame.PlayerHeadItem
import net.casual.championships.common.util.casual

object CasualItems {
    private val register = NamespacedItemRegistryRegister(::casual)

    val DUMMY = register("dummy", ::DummyItem)
    val TINTABLE_DUMMY = register("tintable_dummy", ::TintableDummyItem)
    val FORWARD_FACING_PLAYER_HEAD = register("forward_facing_player_head", ::ForwardFacingPlayerHead)

    val GOLDEN_HEAD = register("golden_head", ::GoldenHeadItem)
    val PLAYER_HEAD = register("player_head", ::PlayerHeadItem)

    internal fun load() {

    }
}