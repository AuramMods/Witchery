package art.arcane.witchery.block;

import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BrambleBlock extends Block {
    public static final BooleanProperty WILD = BooleanProperty.create("wild");
    private static final VoxelShape SHAPE = box(0.8, 0, 0.8, 15.2, 16, 15.2);
    private static final List<BlockPos> SPREAD_COLUMNS = List.of(
            new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0), new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 1), new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 1), new BlockPos(1, 0, -1));

    public BrambleBlock() {
        super(Properties.copy(Blocks.DANDELION).strength(20, 600));
        registerDefaultState(defaultBlockState().setValue(WILD, true));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WILD);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return !level.isEmptyBlock(pos.below());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return state.canSurvive(level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        var stack = new ItemStack(this);
        if (!state.getValue(WILD)) setWild(stack, false);
        return stack;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!(level instanceof ServerLevel server) || !state.getValue(WILD) || tool.is(Items.SHEARS)) return;
        for (BlockPos offset : SPREAD_COLUMNS) spreadInColumn(server, player, pos.offset(offset), state);
    }

    private static void spreadInColumn(ServerLevel level, Player player, BlockPos center, BlockState wild) {
        for (int dy = -1; dy <= 1; dy++) {
            BlockPos pos = center.offset(0, dy, 0);
            BlockState current = level.getBlockState(pos);
            if (!(current.isAir() || current.is(Blocks.SNOW_BLOCK) || current.is(Blocks.GRASS) || current.is(Blocks.FERN))
                    || !wild.canSurvive(level, pos) || level.random.nextInt(2) != 0) continue;
            WitcheryBlockPlacement.place(level, pos, wild, player, Direction.UP);
            if (level.random.nextInt(3) != 0) return;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(WILD) || random.nextInt(2) != 0) return;
        double x = pos.getX() + random.nextFloat();
        double y = pos.getY() + 0.15F + random.nextFloat() * 0.3F + 0.5D;
        double z = pos.getZ() + random.nextFloat();
        level.addParticle(ParticleTypes.PORTAL, x, y, z, 0, -1.2D, 0);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel server)) return;
        if (state.getValue(WILD)) {
            entity.hurt(server.damageSources().cactus(), 1);
        } else if (entity instanceof LivingEntity living && living.isAlive()) {
            // Let the movement packet finish before relocating an entity that stepped inside.
            server.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(WILD)) teleportContacts(level, pos);
    }

    static void teleportContacts(ServerLevel level, BlockPos pos) {
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos), LivingEntity::isAlive)) {
            WitcheryTeleport.randomSurface(living, level, pos, 500);
        }
    }

    public static boolean isWild(ItemStack stack) {
        var state = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return state == null || !"false".equals(state.getString(WILD.getName()));
    }

    public static void setWild(ItemStack stack, boolean wild) {
        stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(WILD.getName(), Boolean.toString(wild));
    }

    public static final class Item extends BlockItem {
        public Item(Block block) {
            super(block, new Properties());
        }

        @Override
        public String getDescriptionId(ItemStack stack) {
            return getDescriptionId() + (isWild(stack) ? ".wild" : ".ender");
        }
    }
}
