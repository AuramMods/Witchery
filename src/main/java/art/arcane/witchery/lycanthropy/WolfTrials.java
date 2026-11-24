package art.arcane.witchery.lycanthropy;

import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerData.WolfmanQuestState;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.HornedHuntsman;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public final class WolfTrials {
    private static final int PACK_LIFETIME = 200;

    private WolfTrials() {}

    public enum Trial {
        AIRBORNE_HUNT("airborne_hunt", 10),
        NIGHT_HOWLS("night_howls", 16),
        TAME_WOLVES("tame_wolves", 6),
        NETHER_HUNT("nether_hunt", 30),
        FINAL_HUNT("final_hunt", 1);

        private final String message;
        private final int required;

        Trial(String message, int required) {
            this.message = message;
            this.required = required;
        }

        public String message() { return message; }

        public int required() { return required; }

        @Nullable
        public static Trial atRank(int rank) {
            return switch (rank) {
                case 5 -> AIRBORNE_HUNT;
                case 6 -> NIGHT_HOWLS;
                case 7 -> TAME_WOLVES;
                case 8 -> NETHER_HUNT;
                case 9 -> FINAL_HUNT;
                default -> null;
            };
        }
    }

    public static void onKilled(LivingEntity victim, ServerPlayer killer, DamageSource source) {
        WitcheryPlayerDataProvider.get(killer).ifPresent(data -> {
            int rank = data.getWerewolfLevel();
            if (rank == 4 && victim instanceof HornedHuntsman && data.getWolfmanQuestState() == WolfmanQuestState.STARTED) {
                data.setWolfmanQuestState(WolfmanQuestState.COMPLETE);
            }
            if (rank < 4 || source.getDirectEntity() != killer || !Lycanthropy.isShifted(killer)) return;
            if (victim.getMobType() != MobType.UNDEAD && !Vampirism.isVampire(victim)) feed(killer, victim);
            if (data.getWolfmanQuestState() != WolfmanQuestState.STARTED) return;
            boolean advances = switch (rank) {
                case 5 -> victim instanceof Enemy && !killer.onGround();
                case 8 -> victim instanceof ZombifiedPiglin && Lycanthropy.isWolfman(killer);
                case 9 -> victim instanceof Villager || victim instanceof Player;
                default -> false;
            };
            if (advances) advance(data, Trial.atRank(rank));
        });
    }

    private static void feed(ServerPlayer killer, LivingEntity victim) {
        ServerLevel world = killer.serverLevel();
        world.sendParticles(DustParticleOptions.REDSTONE, victim.getX(), victim.getY(0.5), victim.getZ(),
                16, 0.4, 0.6, 0.4, 0.05);
        if (world.random.nextInt(3) == 0) {
            world.playSound(null, victim, WitcherySounds.WOLFMAN_EAT.get(), SoundSource.PLAYERS, 1, 1);
        }
        killer.getFoodData().eat(8, 0.8F);
    }

    public static boolean tame(ServerPlayer player, Wolf wolf) {
        if (wolf.isTame() || wolf.isAngry() || !Lycanthropy.isWolf(player)) return false;
        var data = WitcheryPlayerDataProvider.get(player).orElse(null);
        if (data == null || !started(data, 7)) return false;
        if (wolf.getRandom().nextInt(3) == 0) {
            if (ForgeEventFactory.onAnimalTame(wolf, player)) return true;
            wolf.tame(player);
            wolf.getNavigation().stop();
            wolf.setTarget(null);
            wolf.setOrderedToSit(true);
            wolf.setHealth(wolf.getMaxHealth());
            wolf.level().broadcastEntityEvent(wolf, (byte) 7);
            advance(data, Trial.TAME_WOLVES);
        } else {
            wolf.level().broadcastEntityEvent(wolf, (byte) 6);
            if (wolf.getRandom().nextInt(10) == 0) {
                wolf.setTarget(player);
                wolf.setPersistentAngerTarget(player.getUUID());
                wolf.startPersistentAngerTimer();
            }
        }
        return true;
    }

    public static void howl(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || !player.isShiftKeyDown() || player.getXRot() > -89.5F
                || BloodFeeding.paralysis(player, 4)) return;
        var data = WitcheryPlayerDataProvider.get(player).orElse(null);
        if (data == null || data.getSelectedVampirePower() != 0) return;
        ServerLevel world = player.serverLevel();
        if (started(data, 6) && Lycanthropy.isShifted(player) && world.dimensionType().natural() && world.isNight()) {
            playHowlCue(world, player, WitcherySounds.WOLFMAN_HOWL.get(), 1);
            var chunk = player.chunkPosition();
            if (data.markWolfmanQuestChunk(chunk.x, chunk.z)) advance(data, Trial.NIGHT_HOWLS);
            else tell(player, "chunk_visited");
        } else howlAbility(world, player, data);
    }

    private static void howlAbility(ServerLevel world, ServerPlayer player, WitcheryPlayerData data) {
        int rank = data.getWerewolfLevel();
        boolean summonPack = rank >= 8 && Lycanthropy.isWolf(player);
        boolean paralyze = rank >= 7 && Lycanthropy.isWolfman(player);
        if (!summonPack && !paralyze) return;
        if (!player.isCreative() && data.howlCoolingDown(world.getGameTime())) {
            playHowlCue(world, player, SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F);
            tell(player, "howl_cooldown");
            return;
        }
        playHowlCue(world, player, WitcherySounds.WOLFMAN_HOWL.get(), 1);
        data.markHowled();
        if (summonPack) {
            // The original howl rolls the pack limit again before each spawn attempt.
            for (int member = 0; member < 2 + world.random.nextInt(rank - 7); member++) summonPackWolf(world, player);
        } else {
            var targets = world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16),
                    target -> target.isAlive() && !Lycanthropy.isWerewolf(target) && !Vampirism.isVampire(target));
            for (LivingEntity target : targets) {
                int duration = (4 + world.random.nextInt(rank - 6)) * 20;
                target.addEffect(new MobEffectInstance(WitcheryEffects.PARALYSIS.get(), duration, 3));
            }
        }
    }

    private static void playHowlCue(ServerLevel world, ServerPlayer player, SoundEvent sound, float volume) {
        float pitch = 0.4F / (0.8F + (float) world.random.nextDouble() * 0.4F);
        world.playSound(null, player, sound, SoundSource.PLAYERS, volume, pitch);
    }

    private static void summonPackWolf(ServerLevel world, ServerPlayer player) {
        var origin = new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ());
        var at = origin.offset(packOffset(world.random), 0, packOffset(world.random)).mutable();
        if (!world.hasChunkAt(at) || !world.getWorldBorder().isWithinBounds(at)) return;
        int top = Math.min(world.getMaxBuildHeight() - 2, at.getY() + 8);
        while (!world.isEmptyBlock(at) && at.getY() < top) at.move(Direction.UP);
        while (at.getY() > world.getMinBuildHeight() && world.isEmptyBlock(at)) at.move(Direction.DOWN);
        if (!world.getBlockState(at).isFaceSturdy(world, at, Direction.UP)) return;
        at.move(Direction.UP);
        if (!world.isEmptyBlock(at) || !world.isEmptyBlock(at.above())) return;
        Wolf wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.moveTo(at.getX() + 0.5, at.getY() + 0.05, at.getZ() + 0.5, 0, 0);
        if (!world.noCollision(wolf) || world.containsAnyLiquid(wolf.getBoundingBox())) return;
        wolf.setTame(true);
        wolf.setOwnerUUID(player.getUUID());
        wolf.getPersistentData().putBoolean("WITCNoDrops", true);
        wolf.addEffect(new MobEffectInstance(WitcheryEffects.MORTAL_COIL.get(), PACK_LIFETIME));
        if (world.addFreshEntity(wolf)) {
            world.sendParticles(ParticleTypes.SMOKE, wolf.getX(), wolf.getY(0.5), wolf.getZ(), 16, 0.4, 0.4, 0.4, 0.03);
        }
    }

    private static int packOffset(RandomSource random) {
        int offset = random.nextInt(11) - 6;
        return offset > -1 ? offset + 2 : offset;
    }

    private static boolean started(WitcheryPlayerData data, int rank) {
        return data.getWerewolfLevel() == rank && data.getWolfmanQuestState() == WolfmanQuestState.STARTED;
    }

    private static void advance(WitcheryPlayerData data, Trial trial) {
        if (data.getWolfmanQuestCounter() < trial.required()) data.advanceWolfmanQuest();
    }

    private static void tell(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message.witchery.wolfaltar." + key).withStyle(ChatFormatting.RED), true);
    }
}
