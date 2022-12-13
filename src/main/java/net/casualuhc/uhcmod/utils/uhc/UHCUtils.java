package net.casualuhc.uhcmod.utils.uhc;

import carpet.CarpetServer;
import carpet.api.settings.InvalidRuleValueException;
import carpet.api.settings.SettingsManager;
import carpet.fakes.SpawnGroupInterface;
import carpet.utils.SpawnReporter;
import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.utils.gamesettings.GameSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.PlaceableOnWaterItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class UHCUtils {
	private UHCUtils() { }

	public static float calculateMSPT() {
		return (float) (MathHelper.average(UHCMod.SERVER.lastTickLengths) * 1.0E-6F);
	}

	public static boolean detectFlexibleBlockPlacement(World world, BlockPos pos, Direction side, BlockState oldState, ItemUsageContext context) {
		ItemPlacementContext placementContext = new ItemPlacementContext(context);
		boolean fluidsSolid = context.getStack().getItem() instanceof PlaceableOnWaterItem;

		if (!oldState.canReplace(placementContext) || (fluidsSolid && oldState.getFluidState().isStill())) {
			pos = pos.offset(side);
		} else {
			ShapeContext shapeContext = context.getPlayer() == null ? ShapeContext.absent() : ShapeContext.of(context.getPlayer());
			if (!oldState.getOutlineShape(world, pos, shapeContext).isEmpty()) {
				return false;
			}
		}
		for (Direction dir : Direction.values()) {
			BlockState neighbor = world.getBlockState(pos.offset(dir));
			if (!neighbor.canReplace(placementContext) || (fluidsSolid && neighbor.getFluidState().isStill())) {
				return false;
			}
		}
		return true;
	}

	public static void setDescriptor(MinecraftServer server) {
		MutableText description = Text.literal("            §6፠ §bWelcome to Casual UHC! §6፠\n")
			.append(Text.literal("     Yes, it's back! Is your team prepared?").formatted(Formatting.DARK_AQUA));
		server.getServerMetadata().setDescription(description);
	}

	public static void setLobbyGamerules() {
		MinecraftServer server = UHCMod.SERVER;
		GameRules gameRules = server.getGameRules();
		gameRules.get(GameRules.NATURAL_REGENERATION).set(true, server);
		gameRules.get(GameRules.DO_INSOMNIA).set(false, server);
		gameRules.get(GameRules.DO_FIRE_TICK).set(false, server);
		gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, server);
		gameRules.get(GameRules.ANNOUNCE_ADVANCEMENTS).set(true, server);
		gameRules.get(GameRules.FALL_DAMAGE).set(false, server);
		gameRules.get(GameRules.DROWNING_DAMAGE).set(false, server);
		gameRules.get(GameRules.DO_ENTITY_DROPS).set(false, server);
		gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, server);
		gameRules.get(GameRules.DO_TRADER_SPAWNING).set(false, server);
		gameRules.get(GameRules.DO_TILE_DROPS).set(false, server);
		gameRules.get(GameRules.SNOW_ACCUMULATION_HEIGHT).set(0, server);
		gameRules.get(GameRules.RANDOM_TICK_SPEED).set(0, server);
		server.setDifficulty(Difficulty.PEACEFUL, true);
		server.getOverworld().setTimeOfDay(6000); // 6000 = noon
		server.getOverworld().setWeather(999999, 0, false, false);
		server.getOverworld().setSpawnPos(new BlockPos(Config.CURRENT_EVENT.getLobbySpawnPos()), 0);
		server.getOverworld().getWorldBorder().setCenter(0, 0);
		server.getWorlds().forEach(serverWorld -> {
			serverWorld.getWorldBorder().setCenter(0, 0);
			serverWorld.getWorldBorder().setSize(6128);
		});
		GameSettings.PVP.setValue(false);

		try {
			ServerCommandSource source = server.getCommandSource();
			SettingsManager manager = CarpetServer.settingsManager;
			manager.getCarpetRule("commandLog").set(source, "ops");
			manager.getCarpetRule("commandDistance").set(source, "ops");
			manager.getCarpetRule("commandInfo").set(source, "ops");
			manager.getCarpetRule("commandPerimeterInfo").set(source, "ops");
			manager.getCarpetRule("commandProfile").set(source, "ops");
			manager.getCarpetRule("commandScript").set(source, "ops");
			manager.getCarpetRule("lightEngineMaxBatchSize").set(source, "500");
			manager.getCarpetRule("structureBlockLimit").set(source, "256");
			manager.getCarpetRule("fillLimit").set(source, "1000000");
			manager.getCarpetRule("fillUpdates").set(source, "false");
			manager.getCarpetRule("commandInfo").set(source, "ops");
			manager.getCarpetRule("commandInfo").set(source, "ops");
		} catch (InvalidRuleValueException e) {
			UHCMod.LOGGER.error("Failed to set carpet rule", e);
		}
	}

	public static void setUHCGamerules() {
		MinecraftServer server = UHCMod.SERVER;
		GameRules gameRules = server.getGameRules();
		gameRules.get(GameRules.NATURAL_REGENERATION).set(false, server);
		gameRules.get(GameRules.DO_FIRE_TICK).set(true, server);
		gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(true, server);
		gameRules.get(GameRules.FALL_DAMAGE).set(true, server);
		gameRules.get(GameRules.DROWNING_DAMAGE).set(true, server);
		gameRules.get(GameRules.DO_ENTITY_DROPS).set(true, server);
		gameRules.get(GameRules.DO_WEATHER_CYCLE).set(true, server);
		gameRules.get(GameRules.DO_TRADER_SPAWNING).set(true, server);
		gameRules.get(GameRules.DO_TILE_DROPS).set(true, server);
		gameRules.get(GameRules.SNOW_ACCUMULATION_HEIGHT).set(1, server);
		gameRules.get(GameRules.RANDOM_TICK_SPEED).set(3, server);
		server.setDifficulty(Difficulty.HARD, true);
		server.getOverworld().setTimeOfDay(0);

		@SuppressWarnings("DataFlowIssue")
		double ratio = (double) 7 / ((SpawnGroupInterface) (Object) SpawnGroup.MONSTER).getInitialSpawnCap();
		SpawnReporter.mobcap_exponent = 4.0 * Math.log(ratio) / Math.log(2.0);
	}
}
