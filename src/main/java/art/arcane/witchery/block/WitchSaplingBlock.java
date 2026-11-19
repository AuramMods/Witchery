package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class WitchSaplingBlock extends SaplingBlock {
    private static final TreeGrower ROWAN_GROWER = new TreeGrower(WitchWoodType.ROWAN);
    private static final TreeGrower ALDER_GROWER = new TreeGrower(WitchWoodType.ALDER);
    private static final TreeGrower HAWTHORN_GROWER = new TreeGrower(WitchWoodType.HAWTHORN);

    public WitchSaplingBlock(Properties properties) {
        super(ROWAN_GROWER, properties);
        registerDefaultState(defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.ROWAN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WitchWoodType.PROPERTY);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(WitchWoodType.PROPERTY).stack(this);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.75F;
    }

    @Override
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), Block.UPDATE_INVISIBLE);
            return;
        }
        TreeGrower grower = switch (state.getValue(WitchWoodType.PROPERTY)) {
            case ROWAN -> ROWAN_GROWER;
            case ALDER -> ALDER_GROWER;
            case HAWTHORN -> HAWTHORN_GROWER;
        };
        grower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
    }

    private static final class TreeGrower extends AbstractTreeGrower {
        private final ResourceKey<ConfiguredFeature<?, ?>> tree;

        private TreeGrower(WitchWoodType type) {
            tree = ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(Witchery.MODID, type.getSerializedName()));
        }

        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            return tree;
        }
    }
}
