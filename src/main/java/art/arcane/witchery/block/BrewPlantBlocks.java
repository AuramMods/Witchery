package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BrewPlantBlocks {
    private BrewPlantBlocks() {}

    public static final class Web extends Block {
        public Web() {
            super(Properties.copy(Blocks.COBWEB).strength(8).noLootTable());
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            // Vanilla spiders exempt the vanilla Cobweb state from its movement penalty.
            entity.makeStuckInBlock(Blocks.COBWEB.defaultBlockState(), new Vec3(0.25, 0.05, 0.25));
        }

        @Override
        public ItemStack getCloneItemStack(BlockState state, HitResult hit, BlockGetter level, BlockPos pos, Player player) {
            return ItemStack.EMPTY;
        }
    }

    public static final class Vine extends HorizontalDirectionalBlock {
        private static final VoxelShape NORTH = box(0, 0, 14, 16, 16, 16);
        private static final VoxelShape SOUTH = box(0, 0, 0, 16, 16, 2);
        private static final VoxelShape WEST = box(14, 0, 0, 16, 16, 16);
        private static final VoxelShape EAST = box(0, 0, 0, 2, 16, 16);

        public Vine() {
            super(Properties.copy(Blocks.LADDER).strength(0.2F).sound(SoundType.GRASS).noCollission().noLootTable());
            registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            Direction face = context.getClickedFace();
            return defaultBlockState().setValue(FACING, face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite());
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return switch (state.getValue(FACING)) {
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
                default -> NORTH;
            };
        }

        @Override
        public BlockState rotate(BlockState state, Rotation rotation) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        @Override
        public BlockState mirror(BlockState state, Mirror mirror) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }

        @Override
        public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
            return true;
        }

        @Override
        public ItemStack getCloneItemStack(BlockState state, HitResult hit, BlockGetter level, BlockPos pos, Player player) {
            return ItemStack.EMPTY;
        }
    }

    public static final class Cactus extends Block {
        private static final VoxelShape SHAPE = box(1, 0, 1, 15, 16, 15);
        private static final VoxelShape COLLISION = box(1, 0, 1, 15, 15, 15);

        public Cactus() {
            super(Properties.copy(Blocks.CACTUS).strength(0.4F).noLootTable());
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE;
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return COLLISION;
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            return !level.getBlockState(pos.below()).canBeReplaced();
        }

        @Override
        public BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level,
                                      BlockPos pos, BlockPos neighborPos) {
            return side == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            entity.hurt(level.damageSources().cactus(), 1);
        }

        @Override
        public ItemStack getCloneItemStack(BlockState state, HitResult hit, BlockGetter level, BlockPos pos, Player player) {
            return ItemStack.EMPTY;
        }
    }
}
