package art.arcane.witchery.vampirism;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class Vampirism {
    private Vampirism() {
    }

    public static int level(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(WitcheryPlayerData::getVampireLevel).orElse(0);
    }

    public static boolean isInSunlight(ServerPlayer player) {
        var world = player.serverLevel();
        BlockPos crown = BlockPos.containing(player.getX(), Math.ceil(player.getBoundingBox().maxY), player.getZ());
        return world.isDay() && world.dimensionType().hasSkyLight() && world.dimensionType().natural()
                && !world.dimension().equals(WitcheryDimensions.DREAM) && !world.dimension().equals(WitcheryDimensions.TORMENT)
                && world.canSeeSky(crown) && !world.isRainingAt(crown) && !player.isSleeping();
    }

    public static void turn(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getVampireLevel() != 0) return;
            data.setVampireLevel(1);
            data.setBloodPower(125);
            data.setHumanBlood(0);
            sync(player, data);
            player.serverLevel().sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 0.9, player.getZ(),
                    16, 0.5, 0.7, 0.5, 0.04);
        });
    }

    public static void drainHumanBlood(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            data.setHumanBlood(0);
            sync(player, data);
        });
    }

    public static void cure(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getVampireLevel() == 0) return;
            data.setVampireLevel(0);
            data.setBloodPower(0);
            data.setHumanBlood(50);
            data.setVampireVision(false);
            data.setVampireQuestCounter(0);
            data.setSelectedVampirePower(0);
            sync(player, data);
        });
    }

    public static boolean drawBlood(ServerPlayer player, int amount) {
        return WitcheryPlayerDataProvider.get(player).map(data -> {
            if (data.getBloodPower() < amount) return false;
            data.setBloodPower(data.getBloodPower() - amount);
            sync(player, data);
            return true;
        }).orElse(false);
    }

    public static boolean acceptPoppy(ServerPlayer player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> {
            if (!WitcheryServerConfig.ALLOW_VAMPIRE_QUESTS.get() || data.getVampireLevel() != 6
                    || data.getVampireLevelCap() < 7) return false;
            advance(player, data);
            sync(player, data);
            return true;
        }).orElse(false);
    }

    static void feed(ServerPlayer player, WitcheryPlayerData data, int amount, int limit, int remainingVillagerBlood) {
        int change = Math.min(amount, Math.max(0, limit - data.getBloodPower()));
        boolean advanced = false;
        if (change > 0) {
            data.setBloodPower(data.getBloodPower() + change);
            if (WitcheryServerConfig.ALLOW_VAMPIRE_QUESTS.get() && data.getVampireLevel() == 1
                    && data.getBloodPower() == data.getMaxBloodPower()) {
                advance(player, data);
                advanced = true;
            }
        }
        if (remainingVillagerBlood >= 0 && !advanced && WitcheryServerConfig.ALLOW_VAMPIRE_QUESTS.get()
                && data.getVampireLevel() == 2) {
            if (remainingVillagerBlood >= 250 && remainingVillagerBlood <= 280) {
                if (data.getVampireQuestCounter() >= 5) advance(player, data);
                else {
                    data.setVampireQuestCounter(data.getVampireQuestCounter() + 1);
                    player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1, 1);
                }
            } else if (remainingVillagerBlood < 240) {
                data.setVampireQuestCounter(0);
            }
        }
        if (amount > 0 || remainingVillagerBlood >= 0) sync(player, data);
    }

    private static void advance(ServerPlayer player, WitcheryPlayerData data) {
        data.setVampireLevel(data.getVampireLevel() + 1);
        data.setVampireQuestCounter(0);
        data.setSelectedVampirePower(0);
        player.displayClientMessage(Component.translatable("message.witchery.vampire.rank", data.getVampireLevel()), false);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1, 1);
    }

    public static void drinkGoblet(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getVampireLevel() != 0) return;
            if (data.getBloodPower() == 0) turn(player);
            else if (isInSunlight(player)) player.setSecondsOnFire(5);
            else {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 1));
            }
        });
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % 40 != 1 || !player.isAlive() || player.isSpectator()) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            int rank = data.getVampireLevel();
            if (rank == 0) {
                data.setHumanBlood(data.getHumanBlood() + 2);
                return;
            }
            if (player.isEyeInFluid(FluidTags.WATER)) player.setAirSupply(player.getMaxAirSupply());
            player.removeEffect(MobEffects.POISON);
            if (rank >= 2 && data.hasVampireVision()) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
            }
            int previousBlood = data.getBloodPower();
            var hunger = player.getFoodData();
            int feed = Math.min(20 - hunger.getFoodLevel(), previousBlood / 5);
            if (feed > 0) {
                data.setBloodPower(previousBlood - feed * 5);
                hunger.eat(feed, 4);
            }
            if (data.getBloodPower() == 0 && hunger.getFoodLevel() == 0) weaken(player, 8, 1);
            if (!player.getAbilities().instabuild && isInSunlight(player)) {
                if (data.getBloodPower() == 0 && player.tickCount > 400) player.kill();
                if (rank >= 5) {
                    data.setBloodPower(data.getBloodPower() - 60);
                    weaken(player, 3, 0);
                } else {
                    data.setBloodPower(0);
                }
                if (data.getBloodPower() == 0) player.setSecondsOnFire(5);
            }
            if (previousBlood != data.getBloodPower()) sync(player, data);
        });
    }

    private static void weaken(ServerPlayer player, int slowness, int weakness) {
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, slowness, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, weakness, true, false));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, weakness, true, false));
    }

    static void sync(ServerPlayer player, WitcheryPlayerData data) {
        data.bumpSyncRevision();
        WitcheryNetwork.sendPartialExtendedPlayerSync(player, data);
    }
}
