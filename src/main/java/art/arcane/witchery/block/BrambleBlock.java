package art.arcane.witchery.block;

import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BrambleBlock extends Block {
    public static final BooleanProperty WILD = BooleanProperty.create("wild");
    private static final VoxelShape SHAPE = box(0.8, 0, 0.8, 15.2, 16, 15.2);

    public BrambleBlock() {
        super(Properties.copy(Blocks.DANDELION).strength(20, 1000));
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
        if (state.getValue(WILD)) return;
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
