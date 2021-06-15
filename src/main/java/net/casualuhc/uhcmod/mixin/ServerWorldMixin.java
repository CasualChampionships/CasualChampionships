package net.casualuhc.uhcmod.mixin;


import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
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

    @Inject(at = @At("HEAD"), method = "tickEntity", cancellable = true)
    public void tickEntity(Entity entity, CallbackInfo ci) {
        //Checking to see if the player is on the server side and the entity is a player
        if (entity instanceof ServerPlayerEntity) {
            //setting the entity to a player
            ServerPlayerEntity player = (ServerPlayerEntity) entity;
            //ensuring only non spectator players have this effect
            if (!player.isSpectator()) {
                //coord command stuff that checks the players direction and sends the hot bat msg
                if (((ServerPlayerMixinInterface) player).getCoordsBoolean()) {
                    switch (player.getMovementDirection().getHorizontal()) {
                        case 0:
                            ((ServerPlayerMixinInterface) player).setDirection("South");
                            break;
                        case 1:
                            ((ServerPlayerMixinInterface) player).setDirection("West");
                            break;
                        case 2:
                            ((ServerPlayerMixinInterface) player).setDirection("North");
                            break;
                        case 3:
                            ((ServerPlayerMixinInterface) player).setDirection("East");
                            break;
                    }

                    LiteralText hotBar = new LiteralText("(" + player.getBlockPos().getX() + ", " + player.getBlockPos().getY() + ", " +
                            player.getBlockPos().getZ() + ") | Direction: " +
                            ((ServerPlayerMixinInterface) player).getDirection() + " | Distance to WB: " +
                            ((int) player.getEntityWorld().getWorldBorder().getDistanceInsideBorder(entity)) +
                            " | WB radius: " + ((int) player.getEntityWorld().getWorldBorder().getSize() / 2));

                    player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, hotBar));
                }

                //checking to see if the players is behind the world boarder
                if (player.getEntityWorld().getWorldBorder().getDistanceInsideBorder(entity) + 0.2 < 0) {
                    //setting some pre-stuff
                    ((ServerPlayerMixinInterface) player).setAlready(true);
                    ((ServerPlayerMixinInterface) player).setTime(((ServerPlayerMixinInterface) player).getTime() + 1);
                    int bottomBlockY = 0;
                    //trying to get the first non air block to put the arrow on to make it look nicer if they are jumping
                    for (int y = player.getBlockPos().getY(); y > 0; y--) {
                        if (!player.getEntityWorld().getBlockState(new BlockPos(player.getBlockPos().getX(), y, player.getBlockPos().getZ())).equals(Blocks.AIR.getDefaultState())) {
                            bottomBlockY = y + 1;
                            break;
                        }
                    }
                    //checking to see the players is closer the wb on the x than the z
                    if (player.getX() * player.getX() >= player.getZ() * player.getZ()) {
                        //checking to see if the player is closer to the west world border -- handing particle stuff (arrow)
                        if (entity.getX() - player.getEntityWorld().getWorldBorder().getBoundWest() > player.getEntityWorld().getWorldBorder().getBoundEast() - entity.getX()) {
                            ((ServerPlayerMixinInterface) player).getWorldBorder().setCenter(player.getEntityWorld().getWorldBorder().getSize(), player.getEntityWorld().getWorldBorder().getCenterZ());
                            ((ServerPlayerMixinInterface) player).setDirection("west");

                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.2 + (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 + (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.2 + (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 - (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.2 + (float) x / 20, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                            }
                            //checking to see if the player is closer to the east world border -- handing particle stuff (arrow)
                        } else {
                            ((ServerPlayerMixinInterface) player).getWorldBorder().setCenter(-1 * player.getEntityWorld().getWorldBorder().getSize(), player.getEntityWorld().getWorldBorder().getCenterZ());
                            ((ServerPlayerMixinInterface) player).setDirection("east");
                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.8 - (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 + (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.8 - (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 - (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.8 - (float) x / 20, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                            }
                        }
                        //the player is closer to the z axis
                    } else {
                        //checking to see if the player is closer to the north world border -- handing particle stuff (arrow)
                        if (entity.getZ() - player.getEntityWorld().getWorldBorder().getBoundNorth() > player.getEntityWorld().getWorldBorder().getBoundSouth() - entity.getZ()) {
                            ((ServerPlayerMixinInterface) player).getWorldBorder().setCenter(player.getEntityWorld().getWorldBorder().getCenterX(), player.getEntityWorld().getWorldBorder().getSize());
                            ((ServerPlayerMixinInterface) player).setDirection("north");

                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 + (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.2 + (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 - (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.2 + (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.2 + (float) x / 20, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                            }
                            //checking to see if the player is closer to the south world border -- handing particle stuff (arrow)
                        } else {
                            ((ServerPlayerMixinInterface) player).getWorldBorder().setCenter(player.getEntityWorld().getWorldBorder().getCenterX(), -1 * player.getEntityWorld().getWorldBorder().getSize());
                            ((ServerPlayerMixinInterface) player).setDirection("south");
                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 + (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.8 - (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 - (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.8 - (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.8 - (float) x / 20, 0.0f, 0.0f, 0.0f, 0.0f, 1));

                            }
                        }
                    }
                    //sending fake world border packets to the player
                    ((ServerPlayerMixinInterface) player).getWorldBorder().setSize(player.getEntityWorld().getWorldBorder().getSize() + 1);
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(((ServerPlayerMixinInterface) player).getWorldBorder(), WorldBorderS2CPacket.Type.LERP_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(((ServerPlayerMixinInterface) player).getWorldBorder(), WorldBorderS2CPacket.Type.SET_CENTER));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(((ServerPlayerMixinInterface) player).getWorldBorder(), WorldBorderS2CPacket.Type.SET_WARNING_TIME));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(((ServerPlayerMixinInterface) player).getWorldBorder(), WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(((ServerPlayerMixinInterface) player).getWorldBorder(), WorldBorderS2CPacket.Type.SET_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(((ServerPlayerMixinInterface) player).getWorldBorder(), WorldBorderS2CPacket.Type.INITIALIZE));

                    //the big title and sub title msg packets -- sent every 100 game ticks
                    if (((ServerPlayerMixinInterface) player).getTime() % 100 == 0) {
                       LiteralText title = new LiteralText("§cYou're outside the border!");
                        LiteralText subTitle = new LiteralText("§cTravel " + "§a" + ((ServerPlayerMixinInterface) player).getDirection() + " §cto get to safety!");

                        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, title));
                        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, subTitle));
                    }
                    //the player is within the world border so we sent the correct packets one time to remove the fake world border
                } else if (((ServerPlayerMixinInterface) player).getAlready()) {
                    ((ServerPlayerMixinInterface) player).setAlready(false);
                    ((ServerPlayerMixinInterface) player).setTime(99);
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.LERP_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_CENTER));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_WARNING_TIME));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.INITIALIZE));
                }
                //the edge case where the player dies from the world border and is put into spectator mode. Sends correct world border packet one time
            } else if (((ServerPlayerMixinInterface) player).getAlready()) {
                ((ServerPlayerMixinInterface) player).setAlready(false);
                ((ServerPlayerMixinInterface) player).setTime(99);
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.LERP_SIZE));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_CENTER));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_WARNING_TIME));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.SET_SIZE));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(player.getEntityWorld().getWorldBorder(), WorldBorderS2CPacket.Type.INITIALIZE));
            }
        }
    }
}