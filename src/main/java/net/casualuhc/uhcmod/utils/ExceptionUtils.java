package net.casualuhc.uhcmod.utils;

import net.casualuhc.uhcmod.UHCMod;

public class ExceptionUtils {
	// Because I hate try catch everywhere
	public static void runSafe(ThrowableRunnable runnable) {
		try {
			runnable.run();
		}
		catch (Exception e) {
			UHCMod.UHCLogger.error(e);
		}
	}

	@FunctionalInterface
	public interface ThrowableRunnable {
		void run() throws Exception;
	}
}
