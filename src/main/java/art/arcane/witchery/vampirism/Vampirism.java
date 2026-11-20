package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.CaneSwordItem;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class Vampirism {
    private static final TagKey<EntityType<?>> BOOK_BOSSES = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "vampire_book_bosses"));

    private Vampirism() {
    }

    public static int level(Player player) {
        return WitcheryPlayerDataProvider.get(player).map(WitcheryPlayerData::getVampireLevel).orElse(0);
    }

    public static boolean isBoss(LivingEntity entity) {
        return entity.getType().is(BOOK_BOSSES);
    }

    public static boolean isInSunlight(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel world)) return false;
        BlockPos crown = BlockPos.containing(entity.getX(), Math.ceil(entity.getBoundingBox().maxY), entity.getZ());
        return world.isDay() && world.dimensionType().hasSkyLight() && world.dimensionType().natural()
                && !world.dimension().equals(WitcheryDimensions.DREAM) && !world.dimension().equals(WitcheryDimensions.TORMENT)
                && world.canSeeSky(crown) && !world.isRainingAt(crown) && !entity.isSleeping();
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
            data.setVampireUltimate(VampireUltimate.NONE, 0);
            VampireFlight.leave(player, data);
            sync(player, data);
        });
    }

    public static boolean drawBlood(ServerPlayer player, int amount) {
        if (player.getAbilities().instabuild) return true;
        return WitcheryPlayerDataProvider.get(player).map(data -> {
            if (data.getBloodPower() < amount) return false;
            data.setBloodPower(data.getBloodPower() - amount);
            sync(player, data);
            return true;
        }).orElse(false);
    }

    public static boolean drinkCaneReserve(ServerPlayer player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> {
            int reserve = data.getBloodReserve();
            if (reserve == 0 || data.getBloodPower() >= data.getMaxBloodPower()) return false;
            data.setBloodPower(data.getBloodPower() + reserve);
            data.setBloodReserve(0);
            sync(player, data);
            return true;
        }).orElse(false);
    }

    public static boolean acceptPoppy(ServerPlayer player) {
        return WitcheryPlayerDataProvider.get(player).map(data -> {
            if (data.getVampireLevel() != 6 || !canAdvance(data)) return false;
            advance(player, data);
            sync(player, data);
            return true;
        }).orElse(false);
    }

    public static void madeVampireWithOwnBlood(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getVampireLevel() != 9 || !canAdvance(data)) return;
            advance(player, data);
            sync(player, data);
        });
    }

    private static boolean canAdvance(WitcheryPlayerData data) {
        return WitcheryServerConfig.ALLOW_VAMPIRE_QUESTS.get() && data.getVampireLevel() < data.getVampireLevelCap();
    }

    public static void onSunBurst(ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getVampireLevel() == 4 && canAdvance(data)) advanceTrial(player, data, 10);
        });
    }

    public static void onKilled(LivingEntity victim, ServerPlayer killer, DamageSource source) {
        if (source.getDirectEntity() == killer && victim.getType() == EntityType.BLAZE) {
            WitcheryPlayerDataProvider.get(killer).ifPresent(data -> {
                if (data.getVampireLevel() == 5 && canAdvance(data)) advanceTrial(killer, data, 20);
            });
        }
        if (source.is(DamageTypes.PLAYER_ATTACK) && CaneSwordItem.isDrawn(killer.getMainHandItem())) {
            fillCaneReserve(victim, killer);
        }
        boolean incompleteBook = false;
        for (ItemStack stack : killer.getInventory().items) {
            if (stack.getItem() instanceof VampireBookItem) {
                incompleteBook = VampireBookItem.tornPages(stack) < VampireBookItem.MAX_TORN_PAGES;
                break;
            }
        }
        if (!incompleteBook) return;
        EntityType<?> type = victim.getType();
        var random = victim.getRandom();
        if (isBoss(victim)
                || (type == EntityType.ZOMBIFIED_PIGLIN || type == EntityType.ENDERMAN) && random.nextDouble() < 0.09
                || victim instanceof Villager && random.nextDouble() < 0.10
                || victim.getMobType() == MobType.UNDEAD && random.nextDouble() < 0.02) {
            victim.spawnAtLocation(WitcheryIngredient.TORN_VAMPIRE_PAGE.item(), 1);
        }
    }

    private static void fillCaneReserve(LivingEntity victim, ServerPlayer killer) {
        if (victim.getPersistentData().getBoolean("WITCNoDrops")) return;
        int blood = victim instanceof Villager villager ? BloodFeeding.blood(villager)
                : victim instanceof ServerPlayer player ? WitcheryPlayerDataProvider.get(player).map(WitcheryPlayerData::getHumanBlood).orElse(0) : 0;
        if (blood == 0) return;

        WitcheryPlayerDataProvider.get(killer).ifPresent(data -> {
            if (data.getVampireLevel() == 0) return;
            int before = data.getBloodReserve();
            data.setBloodReserve(before + blood);
            if (data.getBloodReserve() != before) sync(killer, data);
        });
    }

    private static void advanceTrial(ServerPlayer player, WitcheryPlayerData data, int required) {
        if (data.getVampireQuestCounter() >= required - 1) {
            advance(player, data);
            sync(player, data);
        } else data.setVampireQuestCounter(data.getVampireQuestCounter() + 1);
    }

    public static boolean canShatter(Player player, BlockState stone) {
        return player.isShiftKeyDown() && !player.isCreative() && !player.isSpectator() && player.getFoodData().getFoodLevel() > 0
                && (stone.is(Blocks.STONE) || stone.is(Blocks.COBBLESTONE) || stone.is(Blocks.NETHERRACK))
                && !player.getMainHandItem().isCorrectToolForDrops(Blocks.STONE.defaultBlockState())
                && WitcheryPlayerDataProvider.get(player).map(data -> data.getVampireLevel() >= 6
                        && data.getCreatureTypeOrdinal() == 0).orElse(false);
    }

    public static void knockBack(ServerPlayer player, LivingEntity victim) {
        if (!player.isShiftKeyDown()) return;
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            if (data.getVampireLevel() < 3 || data.getCreatureTypeOrdinal() != 0) return;
            var look = player.getLookAngle();
            victim.setDeltaMovement(look.x * 1.8, 0.9, look.z * 1.8);
            victim.hurtMarked = true;
        });
    }

    static void feed(ServerPlayer player, WitcheryPlayerData data, int amount, int limit, int remainingVillagerBlood,
                     @Nullable Villager villager) {
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
        if (villager != null && !advanced && WitcheryServerConfig.ALLOW_VAMPIRE_QUESTS.get()
                && (data.getVampireLevel() == 2
                || data.getVampireLevel() == 8 && canAdvance(data) && VampireVillages.isCaged(villager))) {
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
            VampireFlight.tick(player, data);
            if (rank == 3 && player.level().isNight() && WitcheryServerConfig.ALLOW_VAMPIRE_QUESTS.get()) {
                int target = player.getAbilities().instabuild ? 10 : 300;
                if (data.getVampireQuestCounter() < target) {
                    data.setVampireQuestCounter(data.getVampireQuestCounter() + 1);
                } else if (canAdvance(data)) {
                    advance(player, data);
                    sync(player, data);
                }
            }
            if (rank == 7 && canAdvance(data)) {
                var level = player.serverLevel();
                VampireVillages.nearby(level, player.blockPosition()).ifPresent(village -> {
                    if (data.visitVampireVillage(level.dimension().location().toString(), village.x, village.z)) {
                        advanceTrial(player, data, 4);
                        if (data.getVampireLevel() == 7) {
                            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1, 1);
                        }
                    }
                });
            }
            if (player.isEyeInFluid(FluidTags.WATER)) player.setAirSupply(player.getMaxAirSupply());
            player.removeEffect(MobEffects.POISON);
            if (rank >= 2 && data.hasVampireVision()) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, true, false));
            }
            if (player.isOnFire() && player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.hurt(WitcheryDamageTypes.vampireFire(player.level()), 2);
                if (!player.isAlive()) return;
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
                if (data.getBloodPower() == 0 && player.tickCount > 400) {
                    var sunlight = WitcheryDamageTypes.sunlight(player.level());
                    player.getCombatTracker().recordDamage(sunlight, player.getHealth());
                    player.setHealth(0);
                    player.die(sunlight);
                    return;
                }
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
