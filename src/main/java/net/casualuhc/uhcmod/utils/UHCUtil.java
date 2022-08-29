package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;
import net.minecraft.util.math.MathHelper;

public class UHCUtil {
	private UHCUtil() { }

	public static float calculateMSPT() {
		return (float) (MathHelper.average(UHCMod.SERVER.lastTickLengths) * 1.0E-6F);
	}
}
