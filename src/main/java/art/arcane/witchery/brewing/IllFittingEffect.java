package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.Reflection;
import art.arcane.witchery.entity.WitcheryFollower;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class IllFittingEffect extends MobEffect {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    public IllFittingEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int lastTicks = amplifier >= 1 && amplifier <= 3 ? (amplifier + 1) * 15 : 15;
        return duration <= lastTicks && duration % 15 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity wearer, int amplifier) {
        if (!(wearer.level() instanceof ServerLevel level) || wearer instanceof Reflection || wearer instanceof WitcheryFollower) return;
        EquipmentSlot slot = ARMOR[level.random.nextInt(ARMOR.length)];
        ItemStack armor = wearer.getItemBySlot(slot);
        if (armor.isEmpty()) return;
        ItemEntity drop = new ItemEntity(level, wearer.getX(), wearer.getY(), wearer.getZ(), armor.copy());
        drop.setPickUpDelay(5 + 5 * amplifier);
        if (level.addFreshEntity(drop)) wearer.setItemSlot(slot, ItemStack.EMPTY);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
