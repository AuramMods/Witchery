package art.arcane.witchery.entity;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.util.WitcheryTime;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.WitcheryTeleport;
import art.arcane.witchery.world.migration.LegacyVillages;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public final class WitchHunterPursuit {
    private static final String TRIGGER = "WITCHunterTrigger";
    private static final int NOTICE_DELAY = 2_400;

    private WitchHunterPursuit() {}

    public static void noticeBlackMagic(ServerPlayer caster) {
        CompoundTag persistent = caster.getPersistentData();
        CompoundTag data = persistent.getCompound(Player.PERSISTED_NBT_TAG);
        if (data.getLong(TRIGGER) <= 0 && caster.serverLevel().getRandom().nextDouble() < 0.1) {
            data.putLong(TRIGGER, WitcheryTime.epochTicks());
            persistent.put(Player.PERSISTED_NBT_TAG, data);
        }
    }

    public static void tick(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        if (world.getGameTime() % 100 != 1 || player.isSpectator() || world.getDifficulty() == Difficulty.PEACEFUL) return;
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        long now = WitcheryTime.epochTicks();
        long noticed = data.getLong(TRIGGER);
        if (WitcheryTime.isWorldTick(noticed)) {
            // Earlier 1.20.1 builds stored the Overworld game tick under the same key.
            long elapsed = Math.max(0, player.server.overworld().getGameTime() - noticed);
            noticed = now - Math.min(elapsed, NOTICE_DELAY);
            data.putLong(TRIGGER, noticed);
        }
        boolean trace = noticed > 0 && now - noticed >= NOTICE_DELAY && world.getRandom().nextDouble() < 0.01;
        if (!trace && !villageReportsVampire(player)) return;

        data.remove(TRIGGER);
        int spawned = 0;
        for (int attempt = 0; attempt < 3 && spawned < 2; attempt++) {
            if (summon(player)) spawned++;
        }
        if (spawned > 0) player.playNotifySound(WitcherySounds.WITCH_HUNTERS_ARRIVE.get(), SoundSource.HOSTILE, 1, 1);
    }

    private static boolean villageReportsVampire(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        if (Vampirism.level(player) < 10 || player.isCreative()
                || world.getRandom().nextDouble() >= WitcheryServerConfig.VAMPIRE_HUNTER_SPAWN_CHANCE.get()) return false;
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
        RandomSource random = world.getRandom();
        BlockPos column = origin.offset(offset(random), 0, offset(random));
        if (!world.hasChunkAt(column) || !world.getWorldBorder().isWithinBounds(column)) return false;
        BlockPos ground = WitcheryTeleport.findEncounterGround(world, column);
        BlockPos position = ground.above();
        if (world.isOutsideBuildHeight(position.above()) || world.isEmptyBlock(ground)
                || !world.isEmptyBlock(position) || !world.isEmptyBlock(position.above())) return false;

        WitchHunter hunter = WitcheryEntities.WITCH_HUNTER.get().create(world);
        if (hunter == null) return false;
        Vec3 arrival = Vec3.atBottomCenterOf(position).add(0, 0.05, 0);
        hunter.moveTo(arrival.x, arrival.y, arrival.z, 0, 0);
        if (!world.noCollision(hunter)) return false;
        ForgeEventFactory.onFinalizeSpawn(hunter, world, world.getCurrentDifficultyAt(position), MobSpawnType.EVENT, null, null);
        if (!world.addFreshEntity(hunter)) return false;
        hunter.pursue(player);
        WitcheryNetwork.sendWitchHunterArrival(world, arrival, hunter.getBbHeight());
        return true;
    }

    private static int offset(RandomSource random) {
        int shift = random.nextInt(11) - 8;
        return shift > -3 ? shift + 6 : shift;
    }
}
