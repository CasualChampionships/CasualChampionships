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
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
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
            ServerPlayerMixinInterface Iplayer = (ServerPlayerMixinInterface) player;
            World entityWorld = player.getEntityWorld();
            WorldBorder border = entityWorld.getWorldBorder();
            WorldBorder Iborder = Iplayer.getWorldBorder();
            //ensuring only non spectator players have this effect
            if (!player.isSpectator()) {
                //coord command stuff that checks the players direction and sends the hot bat msg
                if (Iplayer.getCoordsBoolean()) {
                    switch (player.getMovementDirection().getHorizontal()) {
                        case 0:
                            Iplayer.setDirection("South");
                            break;
                        case 1:
                            Iplayer.setDirection("West");
                            break;
                        case 2:
                            Iplayer.setDirection("North");
                            break;
                        case 3:
                            Iplayer.setDirection("East");
                            break;
                    }
                    String locationInfo = String.format("(%d, %d, %d) | Direction: %s | Distance to WB: %d | WB radius: %d",
                            player.getBlockPos().getX(),
                            player.getBlockPos().getY(),
                            player.getBlockPos().getZ(),
                            Iplayer.getDirection(),
                            ((int) border.getDistanceInsideBorder(entity)),
                            ((int) border.getSize() / 2)
                    );

                    player.sendMessage(new LiteralText(locationInfo), true);
                }

                //checking to see if the players is behind the world boarder
                if (border.getDistanceInsideBorder(entity) + 0.2 < 0) {
                    //setting some pre-stuff
                    Iplayer.setAlready(true);
                    Iplayer.setTime(Iplayer.getTime() + 1);
                    int bottomBlockY = 0;
                    //trying to get the first non air block to put the arrow on to make it look nicer if they are jumping
                    for (int y = player.getBlockPos().getY(); y > 0; y--) {
                        if (!entityWorld.getBlockState(new BlockPos(player.getBlockPos().getX(), y, player.getBlockPos().getZ())).equals(Blocks.AIR.getDefaultState())) {
                            bottomBlockY = y + 1;
                            break;
                        }
                    }
                    //checking to see the players is closer the wb on the x than the z
                    if (player.getX() * player.getX() >= player.getZ() * player.getZ()) {
                        //checking to see if the player is closer to the west world border -- handing particle stuff (arrow)
                        if (entity.getX() - border.getBoundWest() > border.getBoundEast() - entity.getX()) {
                            Iplayer.getWorldBorder().setCenter(border.getSize(), border.getCenterZ());
                            Iplayer.setDirection("west");

                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.2 + (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 + (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.2 + (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 - (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.2 + (float) x / 20, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                            }
                            //checking to see if the player is closer to the east world border -- handing particle stuff (arrow)
                        }
                        else {
                            Iplayer.getWorldBorder().setCenter(-1 * border.getSize(), border.getCenterZ());
                            Iplayer.setDirection("east");
                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.8 - (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 + (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.8 - (float) x / 45, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5 - (float) x / 30, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.8 - (float) x / 20, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.5, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                            }
                        }
                        //the player is closer to the z axis
                    }
                    else {
                        //checking to see if the player is closer to the north world border -- handing particle stuff (arrow)
                        if (entity.getZ() - border.getBoundNorth() > border.getBoundSouth() - entity.getZ()) {
                            Iplayer.getWorldBorder().setCenter(border.getCenterX(), border.getSize());
                            Iplayer.setDirection("north");

                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 + (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.2 + (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 - (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.2 + (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.2 + (float) x / 20, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                            }
                            //checking to see if the player is closer to the south world border -- handing particle stuff (arrow)
                        } else {
                            Iplayer.getWorldBorder().setCenter(border.getCenterX(), -1 * border.getSize());
                            Iplayer.setDirection("south");
                            for (int x = 0; x < 15; x++) {
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 + (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.8 - (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5 - (float) x / 30, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.8 - (float) x / 45, 0.0f, 0.0f, 0.0f, 0.0f, 1));
                                player.networkHandler.sendPacket(new ParticleS2CPacket(ParticleTypes.END_ROD, false, player.getBlockPos().getX() + 0.5, bottomBlockY + 0.001, player.getBlockPos().getZ() + 0.8 - (float) x / 20, 0.0f, 0.0f, 0.0f, 0.0f, 1));

                            }
                        }
                    }
                    //sending fake world border packets to the player
                    Iplayer.getWorldBorder().setSize(border.getSize() + 1);
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(Iborder, WorldBorderS2CPacket.Type.LERP_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(Iborder, WorldBorderS2CPacket.Type.SET_CENTER));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(Iborder, WorldBorderS2CPacket.Type.SET_WARNING_TIME));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(Iborder, WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(Iborder, WorldBorderS2CPacket.Type.SET_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(Iborder, WorldBorderS2CPacket.Type.INITIALIZE));

                    //the big title and sub title msg packets -- sent every 100 game ticks
                    if (Iplayer.getTime() % 100 == 0) {
                       LiteralText title = new LiteralText("§cYou're outside the border!");
                        LiteralText subTitle = new LiteralText("§cTravel " + "§a" + Iplayer.getDirection() + " §cto get to safety!");

                        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.TITLE, title));
                        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.SUBTITLE, subTitle));
                    }
                    //the player is within the world border so we sent the correct packets one time to remove the fake world border
                } else if (((ServerPlayerMixinInterface) player).getAlready()) {
                    Iplayer.setAlready(false);
                    Iplayer.setTime(99);
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.LERP_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_CENTER));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_WARNING_TIME));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_SIZE));
                    player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.INITIALIZE));
                }
                //the edge case where the player dies from the world border and is put into spectator mode. Sends correct world border packet one time
            } else if (Iplayer.getAlready()) {
                Iplayer.setAlready(false);
                Iplayer.setTime(99);
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.LERP_SIZE));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_CENTER));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_WARNING_TIME));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_WARNING_BLOCKS));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.SET_SIZE));
                player.networkHandler.sendPacket(new WorldBorderS2CPacket(border, WorldBorderS2CPacket.Type.INITIALIZE));
            }
        }
    }
}