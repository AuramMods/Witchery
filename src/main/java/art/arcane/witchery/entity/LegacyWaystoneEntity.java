package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;

public final class LegacyWaystoneEntity extends ItemEntity {
    public LegacyWaystoneEntity(EntityType<? extends ItemEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Item", Tag.TAG_COMPOUND)) {
            var stack = WitcheryIngredient.loadInventoryStack(tag.getCompound("Item"));
            tag = tag.copy();
            tag.put("Item", stack.save(new CompoundTag()));
        }
        super.readAdditionalSaveData(tag);
    }
}
