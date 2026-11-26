package art.arcane.witchery.vampirism;

import art.arcane.witchery.entity.VillageGuard;
import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;

public final class VampireAbilities {
    public static final int DRINK = 1;
    public static final int MESMERIZE = 2;
    public static final int SPEED = 3;
    public static final int BAT = 4;
    public static final int ULTIMATE = 5;

    private VampireAbilities() {}

    public static int maxPower(int rank) {
        if (rank >= 10) return ULTIMATE;
        if (rank >= 7) return BAT;
        if (rank >= 4) return SPEED;
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
        int power = selected(player);
        if (power < MESMERIZE || BloodFeeding.paralysis(player, 4)) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (power > maxPower(data.getVampireLevel())) return;
            if (!data.beginVampireAbility(player.level().getGameTime())) {
                playCue(player, "note.snare");
                return;
            }
            if (power == MESMERIZE && player.isShiftKeyDown()) toggleVision(player, data);
            else if (power == SPEED) accelerate(player, data);
            else if (power == BAT) VampireFlight.toggle(player, data);
            else if (power == ULTIMATE) useUltimate(player, data);
        });
    }

    public static boolean chargeUltimate(ServerPlayer player, VampireUltimate ultimate) {
        return WitcheryPlayerDataProvider.get(player).map(data -> {
            if (data.getVampireLevel() < 10) return false;
            data.setVampireUltimate(ultimate, VampireUltimate.MAX_CHARGES);
            Vampirism.sync(player, data);
            return true;
        }).orElse(false);
    }

    private static void useUltimate(ServerPlayer player, WitcheryPlayerData data) {
        VampireUltimate ultimate = data.getVampireUltimate();
        int charges = data.getVampireUltimateCharges();
        if (charges == 0 || data.getCreatureTypeOrdinal() != 0 || !ultimate.activate(player)) {
            playCue(player, "note.snare");
            return;
        }
        if (ultimate == VampireUltimate.STORM) playCue(player, "random.fizz");
        if (!player.getAbilities().instabuild) {
            data.setVampireUltimate(ultimate, charges - 1);
            Vampirism.sync(player, data);
        }
    }

    static void playCue(ServerPlayer player, String sound) {
        WitcheryNetwork.sendSound(player, sound, player.getX(), player.getY(), player.getZ(), -1, -1);
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
                || living instanceof WereVillager
                || (!(living instanceof Villager || living instanceof VillageGuard)
                && !(living instanceof ServerPlayer victim && player.canHarmPlayer(victim)))
                || living.hasEffect(WitcheryEffects.PARALYSIS.get()) || !Vampirism.drawBlood(player, 50)) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 0.5F);
            return;
        }
        int rank = data.getVampireLevel();
        int clothingBonus = VampireClothingItem.lightPieces(player) >= 3 ? 3 : 0;
        int time = 20 * (5 + rank / 2 + Math.max(0, (rank - 4) / 2) + clothingBonus);
        living.addEffect(new MobEffectInstance(WitcheryEffects.PARALYSIS.get(), time, rank >= 8 ? 5 : 4), player);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                WitcherySounds.HYPNOSIS.get(), SoundSource.PLAYERS, 0.5F, 1);
    }

    private static void toggleVision(ServerPlayer player, WitcheryPlayerData data) {
        boolean active = !data.hasVampireVision();
        data.setVampireVision(active);
        if (active) player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
        else player.removeEffect(MobEffects.NIGHT_VISION);
        Vampirism.sync(player, data);
    }

    private static void accelerate(ServerPlayer player, WitcheryPlayerData data) {
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        int stages = speed == null ? 0 : Mth.ceillog2(speed.getAmplifier() + 1);
        int stackingLimit = (data.getVampireLevel() - 2) / 2;
        if (data.getCreatureTypeOrdinal() != 0 || stages > stackingLimit || !Vampirism.drawBlood(player, 10)) {
            playCue(player, "note.snare");
            return;
        }
        playCue(player, "random.fizz");
        int duration = speed == null ? 200 : speed.getDuration() + 60;
        int amplifier = speed == null ? 1 : 2 * (speed.getAmplifier() + 1) - 1;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, stages + 1, true, true));
    }
}
