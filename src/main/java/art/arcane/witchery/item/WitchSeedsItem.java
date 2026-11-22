package art.arcane.witchery.item;

import art.arcane.witchery.block.WitchCrop;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class WitchSeedsItem extends ItemNameBlockItem {
    private final WitchCrop crop;

    public WitchSeedsItem(WitchCrop crop, Properties properties) {
        super(crop.block(), properties);
        this.crop = crop;
    }

    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return crop == WitchCrop.MINDRAKE ? MindrakeBulbs.LIFESPAN : super.getEntityLifespan(stack, level);
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        return context.getLevel().getBlockState(context.getClickedPos().below()).is(Blocks.FARMLAND)
                ? super.getPlacementState(context) : null;
    }

    public static final class WaterSeeds extends PlaceOnWaterBlockItem {
        public WaterSeeds(WitchCrop crop, Properties properties) {
            super(crop.block(), properties);
        }

        @Override
        public String getDescriptionId() {
            return getOrCreateDescriptionId();
        }
    }
}
