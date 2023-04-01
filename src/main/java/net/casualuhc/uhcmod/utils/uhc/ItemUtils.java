package net.casualuhc.uhcmod.utils.uhc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import static net.minecraft.text.Text.translatable;

public class ItemUtils {
	private static final ItemStack GOLDEN_HEAD = Util.make(generatePlayerHead("PhantomTupac", Config.CURRENT_UHC.getGoldenHeadTexture()), i -> {
		i.setCustomName(translatable("uhc.game.goldenHead").formatted(Formatting.GOLD).styled(s -> s.withItalic(false)));
	});

	/**
	 * Creates an {@link ItemStack} with size 1 with a given name.
	 *
	 * @param item the item to create the stack.
	 * @param string the name to give the stack.
	 * @return the named item stack
	 */
	public static ItemStack literalNamed(Item item, String string) {
		ItemStack stack = new ItemStack(item);
		return stack.setCustomName(Text.literal(string));
	}

	/**
	 * Creates an {@link ItemStack} with size 1 with a translatable key.
	 *
	 * @param item the item to create the stack.
	 * @param string the translatable key.
	 * @param args the arguments for the translatable key.
	 * @return the named item stack.
	 */
	public static ItemStack translatableNamed(Item item, String string, Object... args) {
		ItemStack stack = new ItemStack(item);
		return stack.setCustomName(Text.translatable(string, args));
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

	/**
	 * These are the texture strings for different colours of heads.
	 */
	public static final String RED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2M0ZDdhM2JjM2RlODMzZDMwMzJlODVhMGJmNmYyYmVmNzY4Nzg2MmIzYzZiYzQwY2U3MzEwNjRmNjE1ZGQ5ZCJ9fX0=";
	public static final String BLACK = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY3YTJmMjE4YTZlNmUzOGYyYjU0NWY2YzE3NzMzZjRlZjliYmIyODhlNzU0MDI5NDljMDUyMTg5ZWUifX19";
	public static final String DARK_BLUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWY4NjkwNDhmMDZkMzE4ZTUwNThiY2EwYTg3NmE1OTg2MDc5ZjQ1YTc2NGQxMmFiMzRhNDkzMWRiNmI4MGFkYyJ9fX0=";
	public static final String DARK_GREEN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmNhYzg1MGZiOGZiNjFlNmFmYzYzOGJhZjRkOWJiYjE4NWVlODNlZWJkMWZiODM3NTU1NzYwNGJhM2FjNzg5MCJ9fX0=";
	public static final String DARK_AQUA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTBmYjFmMTNlY2I3ZmJiMmZhNDljZDAzZDM1N2ZhN2UyNzg1MDJiNzg3MzA2MDJhYWExMDY1NWU0ZDk0OTBlMSJ9fX0=";
	public static final String DARK_RED = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGY0ZGMzYzM3NTNiZjViMGI3ZjA4MWNkYjQ5YjgzZDM3NDI4YTEyZTQxODdmNjM0NmRlYzA2ZmFjNTRjZSJ9fX0=";
	public static final String DARK_PURPLE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTQ4NzJjOGY3YTZjZWY3OTg2NDc2OWYwY2ZmNzFlZjIzZjU2NTY1NjdkZTAwMDFhZWYzNmM2YjYxNjJhYjAyZCJ9fX0=";
	public static final String GOLD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTMzMzBmYmVkMzc3YzI0NGY0ODdlNGJjNTY4MmQxNWFmNDBkM2NlNGMzMmVlMDNmYzI0YTdmOTUyZTdkMjlhOSJ9fX0=";
	public static final String GRAY = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmQzY2ZjMjM5MDA2YjI1N2I4YjIwZjg1YTdiZjQyMDI2YzRhZGEwODRjMTQ0OGQwNGUwYzQwNmNlOGEyZWEzMSJ9fX0=";
	public static final String DARK_GRAY = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjA4ZjMyMzQ2MmZiNDM0ZTkyOGJkNjcyODYzOGM5NDRlZTNkODEyZTE2MmI5YzZiYTA3MGZjYWM5YmY5In19fQ==";
	public static final String BLUE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI4MTg4NmZmODliNDdlOTg3NWY5Y2E5MjM0NjhjMTY0ZGUyZjJlNTIyNDBkNTkwZGE5YWUxYTY5ODhiNTM4OSJ9fX0=";
	public static final String GREEN = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTc2OTVmOTZkZGE2MjZmYWFhMDEwZjRhNWYyOGE1M2NkNjZmNzdkZTBjYzI4MGU3YzU4MjVhZDY1ZWVkYzcyZSJ9fX0=";
	public static final String AQUA = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMDdjNzhmM2VlNzgzZmVlY2QyNjkyZWJhNTQ4NTFkYTVjNDMyMzA1NWViZDJmNjgzY2QzZTgzMDJmZWE3YyJ9fX0=";
	public static final String LIGHT_PURPLE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjA1YzE3NjUwZTVkNzQ3MDEwZThiNjlhNmYyMzYzZmQxMWViOTNmODFjNmNlOTliZjAzODk1Y2VmYjkyYmFhIn19fQ==";
	public static final String YELLOW = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjlhMDMwY2EyYjJjNmZlNjdmZTgwOTdkM2NkMjA2OTY5ZmM1YzAwMTdjNjBiNmI0MDk5MGM3NzJhNmYwYWMwYSJ9fX0=";
	public static final String WHITE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjU4Y2FhYjRjOTQxNjM5NWI1OGU4Y2Y2ZjBjYjg2NDhmOWZhZGIxZDgwZDEyMTY2ZGNlNGEzZDI3MjVmYTUzIn19fQ==";
}
