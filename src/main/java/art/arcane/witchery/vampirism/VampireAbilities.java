package art.arcane.witchery.vampirism;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public final class VampireAbilities {
    public static final int DRINK = 1;
    public static final int MESMERIZE = 2;

    private VampireAbilities() {}

    public static int maxPower(int rank) {
        return rank >= 2 ? MESMERIZE : rank == 1 ? DRINK : 0;
    }

    public static int selected(Player player) {
        if (player.getInventory().selected != 0) return 0;
        return WitcheryPlayerDataProvider.get(player).map(WitcheryPlayerData::getSelectedVampirePower).orElse(0);
    }

    public static void select(ServerPlayer player, int power) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (power < 0 || power > maxPower(data.getVampireLevel()) || BloodFeeding.paralysis(player, 4)) return;
            data.setSelectedVampirePower(power);
            Vampirism.sync(player, data);
        });
    }

    public static void activate(ServerPlayer player) {
        if (!player.isShiftKeyDown() || selected(player) != MESMERIZE || BloodFeeding.paralysis(player, 4)) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getVampireLevel() >= 2 && data.beginVampireAbility(player.level().getGameTime())) toggleVision(player, data);
        });
    }

    public static void interact(ServerPlayer player, Entity target) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            int power = selected(player);
            if (power == DRINK && data.getVampireLevel() > 0 && target instanceof LivingEntity living) {
                BloodFeeding.drink(player, living, data);
            } else if (power == MESMERIZE && data.getVampireLevel() >= 2) {
                if (player.isShiftKeyDown()) toggleVision(player, data);
                else mesmerize(player, target, data);
            }
        });
    }

    private static void mesmerize(ServerPlayer player, Entity target, WitcheryPlayerData data) {
        if (!(target instanceof LivingEntity living) || data.getCreatureTypeOrdinal() != 0
                || (!(living instanceof Villager) && !(living instanceof ServerPlayer victim && player.canHarmPlayer(victim)))
                || living.hasEffect(WitcheryEffects.PARALYSIS.get()) || !Vampirism.drawBlood(player, 50)) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 0.5F);
            return;
        }
        int rank = data.getVampireLevel();
        int time = 20 * (5 + rank / 2 + Math.max(0, (rank - 4) / 2));
        living.addEffect(new MobEffectInstance(WitcheryEffects.PARALYSIS.get(), time, rank >= 8 ? 5 : 4), player);
        player.playSound(WitcherySounds.HYPNOSIS.get(), 0.5F, 1);
    }

    private static void toggleVision(ServerPlayer player, WitcheryPlayerData data) {
        boolean active = !data.hasVampireVision();
        data.setVampireVision(active);
        if (active) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        else player.removeEffect(MobEffects.NIGHT_VISION);
        Vampirism.sync(player, data);
    }
}
