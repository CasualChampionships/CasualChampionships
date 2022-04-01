package net.casualuhc.uhcmod.mixin;


import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.HealthBoostStatusEffect;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(at = @At("HEAD"), method = "canPlayerModifyAt", cancellable = true)
    public void canPlayerModifyAt(PlayerEntity player, BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
        ci.setReturnValue(!Objects.requireNonNull(player.getEntityWorld().getServer()).isSpawnProtected(player.getEntityWorld().getServer().getWorld(player.getEntityWorld().getRegistryKey()), pos, player) && Math.abs(pos.getX()) < 30000000 && Math.abs(pos.getZ()) < 30000000);
    }

    @Inject(method = "onPlayerConnected", at = @At("HEAD"))
    private void onPlayerConnected(ServerPlayerEntity player, CallbackInfo ci) {
        Scoreboard scoreboard = UHCMod.UHC_SERVER.getScoreboard();
        player.markHealthDirty();
        if (!GameManager.INSTANCE.isGameActive()) {
            if (!player.hasPermissionLevel(2)) {
                player.changeGameMode(GameMode.ADVENTURE);
                player.teleport(UHCMod.UHC_SERVER.getOverworld(), 0, 253, 0, 0, 0);
                player.sendMessage(new LiteralText("Welcome to Casual UHC!").formatted(Formatting.GOLD), false);
            }
            else {
                player.changeGameMode(GameMode.CREATIVE);
                AbstractTeam team = player.getScoreboardTeam();
                if (team == null) {
                    Team operator = scoreboard.getTeam("Operator");
                    if (operator != null) {
                        scoreboard.addPlayerToTeam(player.getEntityName(), operator);
                    }
                }
            }
        }
        else if (player.getScoreboardTeam() == null || !PlayerUtils.isPlayerPlaying(player)){
            player.changeGameMode(GameMode.SPECTATOR);
        }
        if (player.getScoreboardTeam() == null) {
            Team spectator = scoreboard.getTeam("Spectator");
            if (spectator != null) {
                scoreboard.addPlayerToTeam(player.getEntityName(), spectator);
            }
        }
    }
}