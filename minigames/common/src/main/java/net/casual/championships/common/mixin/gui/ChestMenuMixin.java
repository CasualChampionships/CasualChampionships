package net.casual.championships.common.mixin.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChestMenu.class)
public class ChestMenuMixin {
	@ModifyExpressionValue(
		method = "threeRows(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)Lnet/minecraft/world/inventory/ChestMenu;",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/world/inventory/MenuType;GENERIC_9x3:Lnet/minecraft/world/inventory/MenuType;"
		)
	)
	private static MenuType<?> get9x3MenuType(MenuType<ChestMenu> original) {
		return MenuType.SHULKER_BOX;
	}
}
