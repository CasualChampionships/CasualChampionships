package net.casualuhc.uhcmod;

import net.casualuhc.uhcmod.managers.WorldBorderManager;
import net.casualuhc.uhcmod.recipes.GoldenHeadRecipe;
import net.casualuhc.uhcmod.utils.Config;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.recipe.Recipe;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UHCMod {
    public static final Logger LOGGER = LoggerFactory.getLogger("UHC");
    public static final boolean HAS_CARPET = FabricLoader.getInstance().isModLoaded("carpet");

    public static MinecraftServer SERVER;

    public static void onServerStart(MinecraftServer server) {
        SERVER = server;
        Config.noop();
        WorldBorderManager.noop();
    }

    public static Iterable<Recipe<?>> getCustomRecipes() {
        return List.of(new GoldenHeadRecipe());
    }
}
