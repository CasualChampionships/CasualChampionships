package net.casualuhc.uhcmod;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UHCMod implements ModInitializer {

    public static MinecraftServer UHCServer;
    public static final Logger UHCLogger = LogManager.getLogger("UHC");
    public static float msPerTick = 0.0F;
    public static final boolean isCarpetInstalled = FabricLoader.getInstance().isModLoaded("carpet");

    public static float calculateMSPT() {
        msPerTick = (float) (MathHelper.average(UHCServer.lastTickLengths) * 1.0E-6F);
        return msPerTick;
    }

    @Override
    public void onInitialize() { }
}
