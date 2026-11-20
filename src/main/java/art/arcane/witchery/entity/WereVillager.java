package art.arcane.witchery.entity;

import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;

public final class WereVillager extends Villager {
    private boolean infectious;

    public WereVillager(EntityType<? extends Villager> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount % 100 == 3 && isAlive() && !isBaby()
                && level().getDifficulty() != Difficulty.PEACEFUL
                && Lycanthropy.isFullMoon(level()) && !Lycanthropy.hasWolfsbane(this)) {
            WerewolfVillagers.transform(this, infectious);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Infectious", infectious);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        infectious = tag.getBoolean("Infectious");
    }
}
