package net.casualuhc.uhcmod;

import net.fabricmc.api.ModInitializer;

import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UHCMod implements ModInitializer {

    public static Logger UHCLogger = LogManager.getLogger("UHC");

    public static MinecraftServer UHCServer;

    @Override
    public void onInitialize(){
            System.out.println("Technical Minecraft UHC Ready!");
        }

}
