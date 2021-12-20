package net.casualuhc.uhcmod.mixin;

import com.mojang.authlib.GameProfile;
import net.casualuhc.uhcmod.interfaces.AbstractTeamMixinInterface;
import net.casualuhc.uhcmod.interfaces.ServerPlayerMixinInterface;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.casualuhc.uhcmod.utils.Phase;
import net.casualuhc.uhcmod.utils.PlayerUtils;
import net.casualuhc.uhcmod.utils.TeamUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerMixinInterface {

    @Unique
    private long time = 0;
    @Unique
    private boolean already = true;
    @Unique
    private final WorldBorder worldBorder = new WorldBorder();
    @Unique
    private Direction direction;
    @Unique
    private boolean coordsBoolean = false;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow
    public abstract ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

    @Shadow
    @Final
    public ServerPlayerInteractionManager interactionManager;

    @Shadow protected abstract void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition);

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeathPre(DamageSource source, CallbackInfo ci) {
        // UHCMod.UHCSocketClient.send(this.getDamageTracker().getDeathMessage().getString());
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void onDeathPost(DamageSource source, CallbackInfo ci) {
        if (GameManager.isPhase(Phase.ACTIVE)) {
            UHCDataBase.INSTANCE.updateStats((ServerPlayerEntity) (Object) this);
            this.dropItem(new ItemStack(Items.GOLDEN_APPLE), true, false);
            AbstractTeam team = this.getScoreboardTeam();
            this.interactionManager.changeGameMode(GameMode.SPECTATOR);
            AbstractTeamMixinInterface iTeam = (AbstractTeamMixinInterface) team;
            if (team != null && !TeamUtils.teamHasAlive(team) && !iTeam.isEliminated()) {
                iTeam.setEliminated(true);
                PlayerUtils.forEveryPlayer(playerEntity -> {
                    playerEntity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.MASTER, 0.5f, 1);
                    playerEntity.sendMessage(new LiteralText("%s has been ELIMINATED!".formatted(team.getName())).formatted(team.getColor(), Formatting.BOLD), false);
                });
            }
            if (TeamUtils.isLastTeam()) {
                Phase.END.run();
            }
        }
        if (GameManager.isPhase(Phase.END)) {
            this.interactionManager.changeGameMode(GameMode.SPECTATOR);
        }
    }

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void onDisconnect(CallbackInfo ci) {
        UHCDataBase.INSTANCE.updateStats((ServerPlayerEntity) (Object) this);
    }

    // Getters

    @Override
    public boolean getCoordsBoolean() {
        return this.coordsBoolean;
    }

    @Override
    public boolean getAlready() {
        return this.already;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    // Setters

    @Override
    public void setCoordsBoolean(boolean coordsBoolean) {
        this.coordsBoolean = coordsBoolean;
    }

    @Override
    public void setAlready(boolean already) {
        this.already = already;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }
}
