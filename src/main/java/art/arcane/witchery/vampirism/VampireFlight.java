package art.arcane.witchery.vampirism;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;

public final class VampireFlight {
    private static final int BAT_FORM = 3;
    public static final EntityDimensions BAT_DIMENSIONS = EntityDimensions.fixed(0.3F, 0.6F);
    public static final float BAT_EYE_HEIGHT = 0.48F;

    private VampireFlight() {}

    public static boolean isBat(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(VampireFlight::isBat).orElse(false);
    }

    static boolean isBat(WitcheryPlayerData data) {
        return data.getCreatureTypeOrdinal() == BAT_FORM;
    }

    static void toggle(ServerPlayer player, WitcheryPlayerData data) {
        if (isBat(data)) {
            leave(player, data);
        } else if (data.getCreatureTypeOrdinal() == 0 && !player.isSleeping() && Vampirism.drawBlood(player, 50)) {
            var abilities = player.getAbilities();
            data.rememberFlight(abilities.mayfly, abilities.flying);
            data.setCreatureTypeOrdinal(BAT_FORM);
            player.stopRiding();
            grantFlight(player, true);
            updateForm(player, data);
        } else {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 0.5F);
            return;
        }
        player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + player.getEyeHeight(),
                player.getZ(), 16, 0.3, 0.4, 0.3, 0.04);
        player.playNotifySound(SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 1);
    }

    public static void restore(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (!isBat(data)) return;
            if (data.getVampireLevel() < 7) leave(player, data);
            else {
                grantFlight(player, true);
                player.refreshDimensions();
            }
        });
    }

    static void tick(ServerPlayer player, WitcheryPlayerData data) {
        if (!isBat(data)) return;
        if (!Vampirism.drawBlood(player, 1)) leave(player, data);
        else grantFlight(player, false);
    }

    static void leave(ServerPlayer player, WitcheryPlayerData data) {
        if (!isBat(data)) return;
        data.setCreatureTypeOrdinal(0);
        if (!player.isCreative() && !player.isSpectator()) {
            var abilities = player.getAbilities();
            abilities.mayfly = data.previouslyAllowedToFly();
            abilities.flying = abilities.mayfly && data.previouslyFlying();
            if (player.connection != null) player.onUpdateAbilities();
        }
        player.fallDistance = 0;
        data.rememberFlight(false, false);
        updateForm(player, data);
    }

    private static void grantFlight(ServerPlayer player, boolean startFlying) {
        if (player.isCreative() || player.isSpectator()) return;
        var abilities = player.getAbilities();
        if (!abilities.mayfly || startFlying && !abilities.flying) {
            abilities.flying = startFlying || abilities.flying;
            abilities.mayfly = true;
            if (startFlying && player.onGround()) {
                var velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x, Math.max(velocity.y, 0.2), velocity.z);
                player.setOnGround(false);
                if (player.connection != null) player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
            if (player.connection != null) player.onUpdateAbilities();
        }
    }

    private static void updateForm(ServerPlayer player, WitcheryPlayerData data) {
        Lycanthropy.updateFormAttributes(player, data);
        player.refreshDimensions();
        Vampirism.sync(player, data);
        WitcheryNetwork.broadcastPlayerStyle(player, data);
    }
}
