package net.casualuhc.uhcmod.utils.uhc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import static net.minecraft.text.Text.literal;

public class ItemUtils {
	private static final ItemStack GOLDEN_HEAD = Util.make(generatePlayerHead("PhantomTupac", Config.CURRENT_EVENT.getGoldenHeadTexture()), i -> {
		i.setCustomName(literal("Golden Head").formatted(Formatting.GOLD).styled(s -> s.withItalic(false)));
	});

	/**
	 * Creates an {@link ItemStack} with size 1 with a given name.
	 *
	 * @param item the item to create the stack.
	 * @param string the name to give the stack.
	 * @return the named item stack
	 */
	public static ItemStack named(Item item, String string) {
		ItemStack stack = new ItemStack(item);
		return stack.setCustomName(Text.literal(string));
	}

	/**
	 * Generates a player's head as an item stack with count 1.
	 *
	 * @param playerName the name of the player.
	 * @return the head of the player.
	 */
	public static ItemStack generatePlayerHead(String playerName) {
		return generatePlayerHead(playerName, null);
	}

	/**
	 * Generates a player's head with a given texture with count 1.
	 *
	 * @param playerName the name of the player.
	 * @param texture the texture of the head.
	 * @return the head of the player.
	 * @see #generatePlayerHead(String)
	 */
	public static ItemStack generatePlayerHead(String playerName, String texture) {
		NbtCompound compound = new NbtCompound();
		compound.putString("id", "player_head");
		compound.putByte("Count", (byte) 1);


		if (texture != null) {
			NbtCompound skullData = new NbtCompound();
			skullData.putString("Name", playerName);
			NbtCompound textureCompound = new NbtCompound();
			textureCompound.putString("Value", texture);
			NbtList textures = new NbtList();
			textures.add(textureCompound);
			NbtCompound properties = new NbtCompound();
			properties.put("textures", textures);
			skullData.put("Properties", properties);

			NbtCompound playerData = new NbtCompound();
			playerData.put(SkullItem.SKULL_OWNER_KEY, skullData);
			compound.put("tag", playerData);
		} else {
			NbtCompound playerData = new NbtCompound();
			playerData.putString(SkullItem.SKULL_OWNER_KEY, playerName);
			compound.put("tag", playerData);
		}

		return ItemStack.fromNbt(compound);
	}

	/**
	 * Generates a new Golden Head item stack.
	 *
	 * @return the Golden Head item stack.
	 */
	public static ItemStack generateGoldenHead() {
		return GOLDEN_HEAD.copy();
	}
}
