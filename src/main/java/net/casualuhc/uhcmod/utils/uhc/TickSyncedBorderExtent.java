package net.casualuhc.uhcmod.utils.uhc;

import carpet.helpers.TickSpeed;
import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.border.WorldBorderStage;

/**
 * This class is essentially a copy of {@link WorldBorder.MovingArea}
 * but instead of using real time to lerp the border
 * this class uses the in game ticks.
 * <p>
 * Identity theft of <a href="https://github.com/gnembon/fabric-carpet/pull/1550">this</a>
 * but gnomgnom hasn't accepted my PR so cannot mixin into carpet yet.
 */
public class TickSyncedBorderExtent implements WorldBorder.Area {
	/**
	 * This is like a super jank way of doing it,
	 * but since there are 3 worlds (in vanilla)
	 * we just trigger once every 3 times...
	 */
	private static int counter = 0;

	private final WorldBorder border;
	private final long realDuration;
	private final double tickDuration;
	private final double from;
	private final double to;

	private int ticks;

	public TickSyncedBorderExtent(WorldBorder border, long realDuration, double from, double to) {
		this.border = border;
		this.realDuration = realDuration;
		this.tickDuration = realDuration / 50.0;
		this.from = from;
		this.to = to;
		this.ticks = 0;
	}

	@Override
	public double getBoundWest() {
		int maxSize = this.border.getMaxRadius();
		return MathHelper.clamp(this.border.getCenterX() - this.getSize() / 2.0, -maxSize, maxSize);
	}

	@Override
	public double getBoundEast() {
		int maxSize = this.border.getMaxRadius();
		return MathHelper.clamp(this.border.getCenterX() + this.getSize() / 2.0, -maxSize, maxSize);
	}

	@Override
	public double getBoundNorth() {
		int maxSize = this.border.getMaxRadius();
		return MathHelper.clamp(this.border.getCenterZ() - this.getSize() / 2.0, -maxSize, maxSize);
	}

	@Override
	public double getBoundSouth() {
		int maxSize = this.border.getMaxRadius();
		return MathHelper.clamp(this.border.getCenterZ() + this.getSize() / 2.0, -maxSize, maxSize);
	}

	@Override
	public double getSize() {
		double progress = this.ticks / this.tickDuration;
		return progress < 1.0 ? MathHelper.lerp(progress, this.from, this.to) : this.to;
	}

	@Override
	public double getShrinkingSpeed() {
		return Math.abs(this.from - this.to) / this.tickDuration;
	}

	@Override
	public long getSizeLerpTime() {
		// Rough estimation
		double ms = UHCUtils.calculateMSPT();
		double tps = 1_000.0D / Math.max((TickSpeed.time_warp_start_time != 0) ? 0.0 : TickSpeed.mspt, ms);
		return (long) ((this.tickDuration - this.ticks) / tps * 1_000);
	}

	@Override
	public double getSizeLerpTarget() {
		return this.to;
	}

	@Override
	public WorldBorderStage getStage() {
		return this.to < this.from ? WorldBorderStage.SHRINKING : WorldBorderStage.GROWING;
	}

	@Override
	public void onMaxRadiusChanged() {

	}

	@Override
	public void onCenterChanged() {

	}

	@Override
	public WorldBorder.Area getAreaInstance() {
		if (this.ticks++ % 20 == 0) {
			for (WorldBorderListener listener : this.border.getListeners()) {
				if (!(listener instanceof WorldBorderListener.WorldBorderSyncer)) {
					listener.onInterpolateSize(this.border, this.from, this.to, this.realDuration);
				}
			}
		}

		WorldBorder.Area area = this.ticks >= this.tickDuration ? this.border.new StaticArea(this.to) : this;

		if (area != this && ++counter == 3) {
			EventHandler.onWorldBorderFinishShrinking();
			counter = 0;
		}

		return area;
	}

	@Override
	public VoxelShape asVoxelShape() {
		return VoxelShapes.combineAndSimplify(
			VoxelShapes.UNBOUNDED,
			VoxelShapes.cuboid(
				Math.floor(this.getBoundWest()),
				Double.NEGATIVE_INFINITY,
				Math.floor(this.getBoundNorth()),
				Math.ceil(this.getBoundEast()),
				Double.POSITIVE_INFINITY,
				Math.ceil(this.getBoundSouth())
			),
			BooleanBiFunction.ONLY_FIRST
		);
	}
}
