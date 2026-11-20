package art.arcane.witchery.prediction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public final class PredictedCourtship {
    private static final String COURTSHIP = "WITCCourtship";
    private static final String PLAYER = "Player";
    private static final String EXPIRES = "Expires";

    private PredictedCourtship() {}

    static void start(Villager villager, ServerPlayer player) {
        villager.setVillagerData(villager.getVillagerData().setProfession(VillagerProfession.FARMER));
        CompoundTag courtship = new CompoundTag();
        courtship.putUUID(PLAYER, player.getUUID());
        courtship.putLong(EXPIRES, player.level().getGameTime() + 1_000);
        villager.getPersistentData().put(COURTSHIP, courtship);
    }

    public static void tick(Villager villager, ServerLevel level) {
        CompoundTag data = villager.getPersistentData();
        if (!data.contains(COURTSHIP)) return;
        CompoundTag courtship = data.getCompound(COURTSHIP);
        if (level.getGameTime() >= courtship.getLong(EXPIRES) || !courtship.hasUUID(PLAYER) || villager.getAge() != 0) {
            finish(villager);
            return;
        }
        if (!(level.getPlayerByUUID(courtship.getUUID(PLAYER)) instanceof ServerPlayer mate) || !mate.isAlive()
                || mate.isSpectator() || villager.isSleeping() || villager.isTrading()) return;
        if (villager.getRandom().nextInt(20) == 0) level.broadcastEntityEvent(villager, (byte) 12);
        if (villager.distanceToSqr(mate) > 2.25) {
            if (villager.tickCount % 5 == 0) BehaviorUtils.setWalkAndLookTargetMemories(villager, mate, 0.3F, 0);
            return;
        }
        Villager child = villager.getBreedOffspring(level, villager);
        child.setAge(-24_000);
        child.moveTo(villager.position());
        if (!level.addFreshEntity(child)) return;
        villager.setAge(6_000);
        level.broadcastEntityEvent(child, (byte) 12);
        finish(villager);
    }

    private static void finish(Villager villager) {
        villager.getPersistentData().remove(COURTSHIP);
        villager.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        villager.getNavigation().stop();
    }
}
