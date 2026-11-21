package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.level.Level;

public final class DeathHorse extends SkeletonHorse {
    public DeathHorse(EntityType<? extends SkeletonHorse> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes() {
        return SkeletonHorse.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.3);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(WitcheryIngredient.BINKY_HEAD.item());
    }

    @Override
    protected void dropEquipment() {
        // Binky's head is the only drop, even with a saddle equipped.
    }
}
