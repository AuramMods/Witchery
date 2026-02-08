package art.arcane.witchery.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class PlaceholderBlockEntity extends BlockEntity {
    public PlaceholderBlockEntity(Supplier<? extends BlockEntityType<?>> typeSupplier, BlockPos pos, BlockState state) {
        super(typeSupplier.get(), pos, state);
    }
}
