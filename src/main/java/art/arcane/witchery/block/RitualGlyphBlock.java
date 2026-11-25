package art.arcane.witchery.block;

import art.arcane.witchery.item.BroomItem;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RitualGlyphBlock extends Block {
    public static final int PATTERN_COUNT = 12;
    public static final IntegerProperty PATTERN = IntegerProperty.create("pattern", 0, PATTERN_COUNT - 1);
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 0.25, 16);
    private final String chalk;

    public RitualGlyphBlock(String chalk) {
        super(Properties.copy(Blocks.WHITE_CARPET).noCollission().noLootTable().strength(2.0F, 600)
                .pushReaction(PushReaction.DESTROY));
        this.chalk = chalk;
        registerDefaultState(defaultBlockState().setValue(PATTERN, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PATTERN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return supportsGlyph(level, pos);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    public static boolean supportsGlyph(LevelReader level, BlockPos pos) {
        BlockPos support = pos.below();
        return level.getBlockState(support).isFaceSturdy(level, support, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(WitcheryItems.getRequired(chalk).get());
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player.getMainHandItem().getItem() instanceof BroomItem) level.removeBlock(pos, false);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!chalk.equals("chalkritual")) {
            level.addParticle(chalk.equals("chalkinfernal") ? ParticleTypes.FLAME : ParticleTypes.PORTAL,
                    pos.getX() + 0.4 + 0.2 * random.nextDouble(), pos.getY() + 0.1 + 0.3 * random.nextDouble(),
                    pos.getZ() + 0.4 + 0.2 * random.nextDouble(), 0, 0, 0);
        }
    }
}
