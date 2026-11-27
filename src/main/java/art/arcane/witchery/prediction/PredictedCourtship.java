package art.arcane.witchery.prediction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

import java.util.Comparator;

public final class PredictedCourtship {
    private static final String COURTSHIP = "WITCCourtship";
    private static final String PLAYER = "Player";
    private static final String EXPIRES = "Expires";

    private PredictedCourtship() {}

    static void start(Villager villager, ServerPlayer player) {
        villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.FARMER));
        CompoundTag courtship = new CompoundTag();
        pursue(courtship, player);
        villager.getPersistentData().put(COURTSHIP, courtship);
    }

    public static void tick(Villager villager, ServerLevel level) {
        CompoundTag data = villager.getPersistentData();
        if (!data.contains(COURTSHIP)) return;
        CompoundTag courtship = data.getCompound(COURTSHIP);
        if (courtship.hasUUID(PLAYER)
                && (level.getGameTime() >= courtship.getLong(EXPIRES) || villager.getAge() != 0)) {
            finish(villager, courtship);
            return;
        }
        if (villager.getAge() != 0 || villager.isSleeping() || villager.isTrading()) return;

        if (!courtship.hasUUID(PLAYER)) {
            // The original goal selector checks every third tick while the villager is idle.
            if (villager.tickCount % 3 != 0 || villager.getRandom().nextInt(500) != 0) return;
            level.getEntitiesOfClass(ServerPlayer.class, villager.getBoundingBox().inflate(16, 3, 16), PredictedCourtship::validMate)
                    .stream().min(Comparator.comparingDouble(villager::distanceToSqr))
                    .ifPresent(player -> pursue(courtship, player));
            if (!courtship.hasUUID(PLAYER)) return;
        }
        if (!(level.getPlayerByUUID(courtship.getUUID(PLAYER)) instanceof ServerPlayer mate) || !validMate(mate)) return;

        if (villager.getRandom().nextInt(20) == 0) level.broadcastEntityEvent(villager, (byte) 12);
        BehaviorUtils.lookAtEntity(villager, mate);
        if (villager.distanceToSqr(mate) > 2.25) {
            if (villager.tickCount % 5 == 0) BehaviorUtils.setWalkAndLookTargetMemories(villager, mate, 0.3F, 0);
            return;
        }
        Villager child = villager.getBreedOffspring(level, villager);
        if (child == null) return;
        child.setAge(-24_000);
        child.moveTo(villager.position());
        if (!level.addFreshEntity(child)) return;
        villager.setAge(6_000);
        level.broadcastEntityEvent(child, (byte) 12);
        finish(villager, courtship);
    }

    private static boolean validMate(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator();
    }

    private static void pursue(CompoundTag courtship, ServerPlayer player) {
        courtship.putUUID(PLAYER, player.getUUID());
        courtship.putLong(EXPIRES, player.level().getGameTime() + 1_000);
    }

    private static void finish(Villager villager, CompoundTag courtship) {
        courtship.remove(PLAYER);
        courtship.remove(EXPIRES);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getNavigation().stop();
    }
}
