package art.arcane.witchery.vampirism;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.VillageGuard;
import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;

public final class BloodFeeding {
    private static final String EXTENDED_VILLAGER = "WitcheryExtendedVillager";
    private static final String BLOOD = "Blood";
    private static final String SLEEP_START = "SleepStart";
    public static final int MAX_MORTAL_BLOOD = 500;

    private BloodFeeding() {}

    public static int blood(Villager villager) {
        CompoundTag data = villager.getPersistentData().getCompound(EXTENDED_VILLAGER);
        return data.contains(BLOOD, Tag.TAG_ANY_NUMERIC) ? Mth.clamp(data.getInt(BLOOD), 0, MAX_MORTAL_BLOOD) : MAX_MORTAL_BLOOD;
    }

    private static void storeBlood(Villager villager, int amount) {
        CompoundTag data = villager.getPersistentData().getCompound(EXTENDED_VILLAGER);
        data.putInt(BLOOD, Mth.clamp(amount, 0, MAX_MORTAL_BLOOD));
        villager.getPersistentData().put(EXTENDED_VILLAGER, data);
    }

    public static void tickVillager(Villager villager, ServerLevel level) {
        CompoundTag persistent = villager.getPersistentData();
        if (!villager.isSleeping() && !persistent.contains(EXTENDED_VILLAGER, Tag.TAG_COMPOUND)) return;
        CompoundTag data = persistent.getCompound(EXTENDED_VILLAGER);
        if (villager.isSleeping()) {
            if (!data.contains(SLEEP_START, Tag.TAG_LONG)) {
                data.putLong(SLEEP_START, level.getGameTime());
                persistent.put(EXTENDED_VILLAGER, data);
            }
        } else if (data.contains(SLEEP_START, Tag.TAG_LONG)) {
            long elapsed = level.getGameTime() - data.getLong(SLEEP_START);
            data.remove(SLEEP_START);
            if (elapsed >= 2_400) villager.setHealth(villager.getMaxHealth());
            if (elapsed > 1_200) storeBlood(villager, blood(villager) + 50 * (int) Math.min(10, elapsed / 1_200));
        }
    }

    public static boolean paralysis(LivingEntity target, int minimumAmplifier) {
        var effect = target.getEffect(WitcheryEffects.PARALYSIS.get());
        return effect != null && effect.getAmplifier() >= minimumAmplifier;
    }

    public static int takeFromVillager(Villager villager, int bite) {
        boolean resting = villager.isSleeping() || paralysis(villager, 4);
        int available = blood(villager);
        int amount = Math.min(available, resting ? bite : awakeBite(bite));
        storeBlood(villager, available - amount);
        return amount;
    }

    static void drink(ServerPlayer player, LivingEntity target, WitcheryPlayerData vampire) {
        double reach = target.hasEffect(WitcheryEffects.PARALYSIS.get()) ? 2.1 : 1.3;
        if (player.distanceToSqr(target) > reach * reach || !target.isAlive()) return;
        int normalBite = VampireClothingItem.lightPieces(player) >= 2 ? 15 : 10;
        int bite = vampire.getCreatureTypeOrdinal() == 0 ? normalBite : 2;
        int amount;
        int remainingVillagerBlood = -1;
        int maximum = vampire.getMaxBloodPower();
        if (target instanceof Villager villager) {
            boolean resting = villager.isSleeping() || paralysis(villager, 4);
            amount = takeFromVillager(villager, bite);
            int remaining = blood(villager);
            remainingVillagerBlood = remaining;
            if (remaining < 250) villager.hurt(WitcheryDamageTypes.vampireBite(player), 1.3F);
            else if (!resting) villager.hurt(WitcheryDamageTypes.vampireBite(player), 0.1F);
            WitcheryNetwork.sendBloodTarget(player, villager.getId(), remaining);
            alertGuards(player, villager);
        } else if (target instanceof VillageGuard guard) {
            int before = guard.getBlood();
            amount = Math.min(before, awakeBite(bite));
            guard.setBlood(before - amount);
            if (before < 250) guard.hurt(WitcheryDamageTypes.vampireBite(player), 2);
            else if (!paralysis(guard, 4)) guard.hurt(WitcheryDamageTypes.vampireBite(player), 0.5F);
            WitcheryNetwork.sendBloodTarget(player, guard.getId(), guard.getBlood());
            alertGuards(player, guard);
        } else if (target instanceof ServerPlayer victim) {
            if (!player.canHarmPlayer(victim)) return;
            amount = WitcheryPlayerDataProvider.get(victim).map(data -> takeFromPlayer(player, victim, data, bite)).orElse(0);
        } else if (target instanceof Animal) {
            target.hurt(WitcheryDamageTypes.vampireBite(player), 1);
            amount = 2;
            maximum = Mth.ceil(maximum / 4.0F);
        } else return;
        Vampirism.feed(player, vampire, amount, maximum, remainingVillagerBlood, target instanceof Villager villager ? villager : null);
        GlassGobletItem.bloodParticles(player.serverLevel(), target);
    }

    private static void alertGuards(ServerPlayer player, LivingEntity victim) {
        var bounds = victim.getBoundingBox().inflate(16, 8, 16);
        for (VillageGuard guard : player.serverLevel().getEntitiesOfClass(VillageGuard.class, bounds)) {
            if (!guard.hasEffect(WitcheryEffects.PARALYSIS.get()) && guard.canAttack(player)
                    && guard.getSensing().hasLineOfSight(victim)) {
                guard.setTarget(player);
            }
        }
    }

    private static int takeFromPlayer(ServerPlayer attacker, ServerPlayer victim, WitcheryPlayerData data, int bite) {
        if (data.getVampireLevel() > 0) return 0;
        int before = data.getHumanBlood();
        int remaining = Math.max(0, before - (victim.isSleeping() ? bite : awakeBite(bite)));
        data.setHumanBlood(remaining);
        if (remaining < 250) victim.hurt(WitcheryDamageTypes.vampireBite(attacker), 1);
        else if (!victim.isSleeping()) victim.hurt(WitcheryDamageTypes.vampireBite(attacker), 0.1F);
        WitcheryNetwork.sendBloodTarget(attacker, victim.getId(), remaining);
        Vampirism.sync(victim, data);
        return before - remaining;
    }

    private static int awakeBite(int bite) {
        return Mth.ceil(0.66F * bite);
    }

    public static void shareVisibleBlood(ServerPlayer viewer, int entityId) {
        if (Vampirism.level(viewer) == 0) return;
        var target = viewer.serverLevel().getEntity(entityId);
        if (!(target instanceof LivingEntity living) || target == viewer || viewer.distanceToSqr(target) > 30.25
                || !living.isAlive() || !viewer.hasLineOfSight(target)) return;
        int amount = target instanceof Villager villager ? blood(villager) : target instanceof VillageGuard guard ? guard.getBlood()
                : target instanceof ServerPlayer player
                ? WitcheryPlayerDataProvider.get(player).map(data -> data.getVampireLevel() == 0 ? data.getHumanBlood() : -1).orElse(-1) : -1;
        WitcheryNetwork.sendBloodTarget(viewer, entityId, amount);
    }
}
