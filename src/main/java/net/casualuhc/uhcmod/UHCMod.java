package net.casualuhc.uhcmod;

import net.casualuhc.uhcmod.events.ChristmasUHC;
import net.casualuhc.uhcmod.features.UHCAdvancements;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.managers.PlayerManager;
import net.casualuhc.uhcmod.managers.TeamManager;
import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.casualuhc.uhcmod.utils.scheduling.Scheduler;
import net.casualuhc.uhcmod.utils.uhc.Config;
import net.casualuhc.uhcmod.utils.uhc.Event;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UHCMod implements ModInitializer {
    private static final Map<String, Event> EVENTS = new HashMap<>();

    public static final Logger LOGGER = LoggerFactory.getLogger("UHC");

    public static MinecraftServer SERVER;

    public static void onServerStart(MinecraftServer server) {
        SERVER = server;
        Config.noop();
        GameManager.noop();
        UHCAdvancements.noop();
        WorldBorderManager.noop();
        PlayerManager.noop();
        TeamManager.noop();
        Scheduler.noop();

        Config.CURRENT_EVENT.load();
    }

    public static void addEvent(String string, Event event) {
        EVENTS.put(string, event);
    }

    @Nullable
    public static Event getEvent(String name) {
        return EVENTS.get(name);
    }

    @Override
    public void onInitialize() {
        addEvent("christmas", new ChristmasUHC());
    }
}
