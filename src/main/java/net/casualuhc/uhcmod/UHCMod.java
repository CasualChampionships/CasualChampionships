package net.casualuhc.uhcmod;

import net.casualuhc.uhcmod.utils.Config;
import net.casualuhc.uhcmod.utils.Networking.UHCWebSocketClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;

public class UHCMod implements ModInitializer {

    public static final UHCWebSocketClient UHCSocketClient = new UHCWebSocketClient(URI.create("ws://localhost:12345"));
    public static final Logger UHCLogger = LogManager.getLogger("UHC");
    public static final boolean isCarpetInstalled = FabricLoader.getInstance().isModLoaded("carpet");

    public static MinecraftServer UHC_SERVER;
    public static float msPerTick = 0.0F;

    public static float calculateMSPT() {
        msPerTick = (float) (MathHelper.average(UHC_SERVER.lastTickLengths) * 1.0E-6F);
        return msPerTick;
    }

    public static void onServerStart(MinecraftServer server) {
        UHC_SERVER = server;
        Config.readConfigs();
    }

    @Override
    public void onInitialize() {
        // UHCSocketClient.connect();
    }
}
