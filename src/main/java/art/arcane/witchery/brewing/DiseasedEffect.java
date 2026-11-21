package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class DiseasedEffect extends TrackedBrewEffect {
    public DiseasedEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, "22653B89-116E-49DC-9B6B-9971489B5BE5", 2,
                AttributeModifier.Operation.ADDITION);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return -0.5 * (Math.min(amplifier, 1) + 1);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 40 == 4;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level) || level.random.nextInt(3) != 0) return;
        var pos = target.blockPosition();
        var below = pos.below();
        if (level.isEmptyBlock(pos) && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            BrewTerrain.setBlock(target, level, pos, WitcheryBlocks.getRequired("disease").get().defaultBlockState(), Direction.UP);
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
