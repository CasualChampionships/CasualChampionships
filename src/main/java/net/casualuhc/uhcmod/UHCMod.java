package net.casualuhc.uhcmod;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UHCMod implements ModInitializer {

    public static Logger UHCLogger = LogManager.getLogger("UHC");

    @Override
    public void onInitialize(){
            System.out.println("Technical Minecraft UHC Ready!");
        }

}
