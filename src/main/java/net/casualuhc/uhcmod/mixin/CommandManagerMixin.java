package net.casualuhc.uhcmod.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.casualuhc.uhcmod.command.*;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static net.minecraft.server.command.CommandManager.RegistrationEnvironment;

@Mixin(CommandManager.class)
public class CommandManagerMixin {
    @Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void register(RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
        PosCommand.register(this.dispatcher);
        SpectatorCommand.register(this.dispatcher);
        CoordsCommand.register(this.dispatcher);
        UHCCommand.register(this.dispatcher);
        ReadyCommand.register(this.dispatcher);
        TeamGlowingCommand.register(this.dispatcher);
        KitCommand.register(this.dispatcher);
        FullBrightCommand.register(this.dispatcher);
        DuelCommand.register(this.dispatcher);
        MinesweeperCommand.register(this.dispatcher);
    }
}
