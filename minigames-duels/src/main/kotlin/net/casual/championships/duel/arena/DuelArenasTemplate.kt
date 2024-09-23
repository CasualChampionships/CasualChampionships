package net.casual.championships.duel.arena

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.championships.common.arena.ArenaTemplate
import net.minecraft.world.item.ItemStack

class DuelArenasTemplate(
    val name: String,
    val display: ItemStack,
    private val small: ArenaTemplate,
    private val medium: ArenaTemplate,
    private val large: ArenaTemplate
) {
    fun getArenaTemplateFor(size: ArenaSize): ArenaTemplate {
        return when (size) {
            ArenaSize.Small -> this.small
            ArenaSize.Medium -> this.medium
            ArenaSize.Large -> this.large
        }
    }

    companion object {
        val CODEC: Codec<DuelArenasTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("name").forGetter(DuelArenasTemplate::name),
                ItemStack.SINGLE_ITEM_CODEC.fieldOf("display").forGetter(DuelArenasTemplate::display),
                ArenaTemplate.CODEC.fieldOf("small").forGetter(DuelArenasTemplate::small),
                ArenaTemplate.CODEC.fieldOf("medium").forGetter(DuelArenasTemplate::medium),
                ArenaTemplate.CODEC.fieldOf("large").forGetter(DuelArenasTemplate::large)
            ).apply(instance, ::DuelArenasTemplate)
        }
    }
}