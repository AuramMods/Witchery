package art.arcane.witchery.block;

import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

public final class FrozenPressurePlateBlock extends PressurePlateBlock {
    private final boolean ice;

    public FrozenPressurePlateBlock(boolean ice) {
        super(Sensitivity.EVERYTHING, Properties.copy(Blocks.STONE_PRESSURE_PLATE)
                .strength(ice ? 2 : 0.2F, ice ? 5 : 0.2F), BlockSetType.STONE);
        this.ice = ice;
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        if (!ice) return super.getSignalStrength(level, pos);
        return level.getEntitiesOfClass(LivingEntity.class, TOUCH_AABB.move(pos),
                wearer -> EntitySelector.NO_SPECTATORS.test(wearer)
                        && WitchClothingItem.style(wearer.getItemBySlot(EquipmentSlot.FEET))
                        == WitchClothingItem.Style.ICY_SLIPPERS).isEmpty() ? 0 : 15;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return super.canSurvive(state, level, pos)
                || level.getBlockState(pos.below()).is(WitcheryBlocks.getRequired("icefence").get());
    }
}
