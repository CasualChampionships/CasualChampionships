package net.casualuhc.uhcmod;

import net.casualuhc.arcade.events.EventHandler;
import net.casualuhc.arcade.events.server.ServerLoadedEvent;
import net.casualuhc.arcade.recipes.RecipeHandler;
import net.casualuhc.arcade.scheduler.MinecraftTimeUnit;
import net.casualuhc.arcade.scheduler.Scheduler;
import net.casualuhc.uhcmod.features.GoldenHeadRecipe;
import net.casualuhc.uhcmod.managers.*;
import net.casualuhc.uhcmod.uhcs.ChristmasUHC;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.uhcs.EasterUHC;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.casualuhc.uhcmod.utils.uhc.GenericEvents;
import net.casualuhc.uhcmod.utils.uhc.UHC;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.lang.JLang;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UHCMod implements ModInitializer {
    private static final Map<String, UHC> EVENTS = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("UHC");

    public static MinecraftServer SERVER;

    static {
        GenericEvents.noop();

        EventHandler.register(ServerLoadedEvent.class, event -> {
            SERVER = event.getServer();
            Config.noop();

            // Fully load everything
            Scheduler.schedule(0, MinecraftTimeUnit.Ticks, () -> {
                UHCManager.noop();
                UHCAdvancements.noop();
                WorldBorderManager.noop();
                PlayerManager.noop();
                TeamManager.noop();
                RecipeManager.registerRecipes();

                Config.CURRENT_UHC.load();
            });
        });
    }

    public static void addUHCEvent(String string, UHC UHC) {
        EVENTS.put(string, UHC);
    }

    @Nullable
    public static UHC getUHC(String name) {
        return EVENTS.get(name);
    }

    @Override
    public void onInitialize() {
        addUHCEvent("christmas", new ChristmasUHC());
        addUHCEvent("easter", new EasterUHC());
    }
}
