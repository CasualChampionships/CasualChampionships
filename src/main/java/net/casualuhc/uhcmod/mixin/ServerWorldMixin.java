package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.GameSetting.GameSettings;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.Scheduler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(at = @At("HEAD"), method = "canPlayerModifyAt", cancellable = true)
    public void canPlayerModifyAt(PlayerEntity player, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Ignore spawn protection
        cir.setReturnValue(Math.abs(pos.getX()) < 30000000 && Math.abs(pos.getZ()) < 30000000);
    }

    @Inject(method = "onPlayerConnected", at = @At("HEAD"))
    private void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        if (GameSettings.TESTING.getValue()) {
            player.changeGameMode(GameMode.SURVIVAL);
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("Welcome to UHC Test!").formatted(Formatting.GOLD)));
            player.sendMessage(Text.literal("Use /kit to get some items"));
            player.sendMessage(Text.literal("You may also use /kill"));
        } else {
            Scoreboard scoreboard = UHCMod.SERVER.getScoreboard();
            if (!GameManager.isGameActive()) {
                if (!player.hasPermissionLevel(2)) {
                    player.changeGameMode(GameMode.ADVENTURE);
                    player.teleport(UHCMod.SERVER.getOverworld(), 0, 253, 0, 0, 0);
                    player.sendMessage(Text.literal("Welcome to Casual UHC!").formatted(Formatting.GOLD), false);
                } else {
                    player.changeGameMode(GameMode.CREATIVE);
                    AbstractTeam team = player.getScoreboardTeam();
                    if (team == null) {
                        Team operator = scoreboard.getTeam("Operator");
                        if (operator != null) {
                            scoreboard.addPlayerToTeam(player.getEntityName(), operator);
                        }
                    }
                }
            } else if (player.getScoreboardTeam() == null || !PlayerUtils.isPlayerPlaying(player)){
                player.changeGameMode(GameMode.SPECTATOR);
            }
            if (player.getScoreboardTeam() == null) {
                Team spectator = scoreboard.getTeam("Spectator");
                if (spectator != null) {
                    scoreboard.addPlayerToTeam(player.getEntityName(), spectator);
                }
            }
        }

        // idk...
        Scheduler.schedule(20, player::markHealthDirty);
    }
}
