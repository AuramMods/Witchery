package art.arcane.witchery.vampirism;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.integration.PlayerAbilityLibCompat;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
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
        boolean leaving = isBat(data);
        if (!leaving && (data.getCreatureTypeOrdinal() != 0 || player.isSleeping() || !Vampirism.drawBlood(player, 50))) {
            VampireAbilities.playCue(player, "note.snare");
            return;
        }
        VampireAbilities.playCue(player, "random.fizz");
        if (leaving) {
            leave(player, data);
            return;
        }
        var abilities = player.getAbilities();
        data.rememberFlight(abilities.mayfly, abilities.flying);
        data.setCreatureTypeOrdinal(BAT_FORM);
        player.stopRiding();
        grantFlight(player, true);
        updateForm(player, data);
    }

    public static void restore(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (!isBat(data)) PlayerAbilityLibCompat.setBatFlight(player, false);
            else if (data.getVampireLevel() < 7) leave(player, data);
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
        boolean managed = PlayerAbilityLibCompat.setBatFlight(player, false);
        if (!player.isCreative() && !player.isSpectator()) {
            var abilities = player.getAbilities();
            if (!managed) abilities.mayfly = data.previouslyAllowedToFly();
            abilities.flying = abilities.mayfly && (data.previouslyFlying() || managed && abilities.flying);
            if (player.connection != null) player.onUpdateAbilities();
        }
        player.fallDistance = 0;
        data.rememberFlight(false, false);
        updateForm(player, data);
    }

    private static void grantFlight(ServerPlayer player, boolean startFlying) {
        if (player.isCreative() || player.isSpectator()) return;
        var abilities = player.getAbilities();
        boolean alreadyAllowed = abilities.mayfly;
        // Register our own grant even when a second source already lets this player fly.
        if ((startFlying || !alreadyAllowed) && !PlayerAbilityLibCompat.setBatFlight(player, true)) {
            abilities.mayfly = true;
        }
        if (abilities.mayfly && (!alreadyAllowed || startFlying && !abilities.flying)) {
            abilities.flying = startFlying || abilities.flying;
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
