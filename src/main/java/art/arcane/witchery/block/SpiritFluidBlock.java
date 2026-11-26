package art.arcane.witchery.block;

import art.arcane.witchery.entity.CreatureAlignment;
import art.arcane.witchery.entity.Nightmare;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

import java.util.function.Supplier;

public final class SpiritFluidBlock extends LiquidBlock {
    private final boolean flowingSpirit;

    public SpiritFluidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties, boolean flowingSpirit) {
        super(fluid, properties);
        this.flowingSpirit = flowingSpirit;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        if (flowingSpirit && state.getFluidState().isSource() && SpiritPortalBlock.tryIgnite(level, pos)) {
            return;
        }
        super.onPlace(state, level, pos, previous, moved);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide()) {
            return;
        }
        if (entity instanceof LivingEntity living) {
            boolean undead = living instanceof Player ? CreatureAlignment.undead(living) : living.getMobType() == MobType.UNDEAD;
            boolean hostileSpirit = undead || CreatureAlignment.demonic(living)
                    || flowingSpirit && living instanceof Nightmare;
            MobEffect effect = flowingSpirit != hostileSpirit ? MobEffects.REGENERATION : MobEffects.WEAKNESS;
            if (!living.hasEffect(effect)) {
                int duration = flowingSpirit && hostileSpirit ? 300 : 100;
                living.addEffect(new MobEffectInstance(effect, duration, 1));
            }
        } else if (flowingSpirit && entity instanceof ItemEntity drop && drop.getItem().is(WitcheryIngredient.DISTURBED_COTTON.item())) {
            drop.setItem(new ItemStack(WitcheryBlocks.getRequired("somniancotton").get(), drop.getItem().getCount()));
        }
    }
}
