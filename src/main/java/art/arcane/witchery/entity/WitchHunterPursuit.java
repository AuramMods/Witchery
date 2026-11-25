package art.arcane.witchery.entity;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.migration.LegacyVillages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public final class WitchHunterPursuit {
    private static final String TRIGGER = "WITCHunterTrigger";
    private static final int NOTICE_DELAY = 2_400;

    private WitchHunterPursuit() {}

    public static void noticeBlackMagic(ServerPlayer caster) {
        CompoundTag persistent = caster.getPersistentData();
        CompoundTag data = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (data.getLong(TRIGGER) <= 0 && caster.getRandom().nextFloat() < 0.1F) {
            data.putLong(TRIGGER, Math.max(1, caster.server.overworld().getGameTime()));
            persistent.put(Player.PERSISTED_NBT_TAG, data);
        }
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % 100 != 1 || player.isSpectator() || player.serverLevel().getDifficulty() == Difficulty.PEACEFUL) return;
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        long now = player.server.overworld().getGameTime();
        long noticed = data.getLong(TRIGGER);
        boolean trace = noticed > 0 && now - noticed >= NOTICE_DELAY && player.getRandom().nextFloat() < 0.01F;
        if (!trace && !villageReportsVampire(player)) return;

        data.remove(TRIGGER);
        int spawned = 0;
        for (int attempt = 0; attempt < 3 && spawned < 2; attempt++) {
            if (summon(player)) spawned++;
        }
        if (spawned > 0) player.playNotifySound(WitcherySounds.WITCH_HUNTERS_ARRIVE.get(), SoundSource.HOSTILE, 1, 1);
    }

    private static boolean villageReportsVampire(ServerPlayer player) {
        if (Vampirism.level(player) < 10 || player.isCreative()
                || player.getRandom().nextDouble() >= WitcheryServerConfig.VAMPIRE_HUNTER_SPAWN_CHANCE.get()) return false;
        ServerLevel world = player.serverLevel();
        var legacy = LegacyVillages.nearest(world, player.blockPosition(), 128).orElse(null);
        var residents = world.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(128, 16, 128),
                villager -> villager.isAlive() && reportsPlayer(world, villager, player, legacy));
        return !residents.isEmpty() && world.getEntitiesOfClass(WitchHunter.class, player.getBoundingBox().inflate(64, 16, 64)).isEmpty();
    }

    private static boolean reportsPlayer(ServerLevel world, Villager resident, ServerPlayer player,
                                         @Nullable LegacyVillages.Village legacy) {
        int gossip = resident.getPlayerReputation(player);
        if (legacy != null && legacy.contains(resident.blockPosition(), 0)) {
            return gossip + legacy.reputation(player.getGameProfile().getName()) < -1;
        }
        return gossip < -1 && world.isVillage(resident.blockPosition());
    }

    private static boolean summon(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        BlockPos origin = player.blockPosition();
        RandomSource random = player.getRandom();
        var position = origin.offset(offset(random), 0, offset(random)).mutable();
        if (!world.hasChunkAt(position)) return false;
        int top = Math.min(world.getMaxBuildHeight() - 2, origin.getY() + 8);
        while (position.getY() < top && !world.isEmptyBlock(position)) position.move(Direction.UP);
        while (position.getY() > world.getMinBuildHeight() && world.isEmptyBlock(position.below())) position.move(Direction.DOWN);
        if (!world.isEmptyBlock(position) || !world.isEmptyBlock(position.above())
                || !world.getBlockState(position.below()).isFaceSturdy(world, position.below(), Direction.UP)) return false;

        WitchHunter hunter = WitcheryEntities.WITCH_HUNTER.get().create(world);
        if (hunter == null) return false;
        hunter.moveTo(position.getX() + 0.5, position.getY() + 0.05, position.getZ() + 0.5, 0, 0);
        if (!world.noCollision(hunter)) return false;
        ForgeEventFactory.onFinalizeSpawn(hunter, world, world.getCurrentDifficultyAt(position), MobSpawnType.EVENT, null, null);
        if (!world.addFreshEntity(hunter)) return false;
        hunter.pursue(player);
        world.sendParticles(ParticleTypes.SMOKE, hunter.getX(), hunter.getY(0.5), hunter.getZ(), 16, 0.5, 0.5, 0.5, 0.01);
        return true;
    }

    private static int offset(RandomSource random) {
        int shift = random.nextInt(11) - 8;
        return shift > -3 ? shift + 6 : shift;
    }
}
