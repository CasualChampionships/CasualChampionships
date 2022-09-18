package net.casualuhc.uhcmod.interfaces;

import net.minecraft.server.MinecraftServer;

public interface IntRuleMixinInterface {
	void setIntegerValue(int newValue, MinecraftServer server);
}
