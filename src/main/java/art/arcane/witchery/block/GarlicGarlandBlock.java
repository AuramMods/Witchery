package art.arcane.witchery.block;

import art.arcane.witchery.entity.Vampire;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GarlicGarlandBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape NORTH = box(1.6, 12.8, 13.6, 14.4, 16, 16);
    private static final VoxelShape EAST = box(0, 12.8, 1.6, 2.4, 16, 14.4);
    private static final VoxelShape SOUTH = box(1.6, 12.8, 0, 14.4, 16, 2.4);
    private static final VoxelShape WEST = box(13.6, 12.8, 1.6, 16, 16, 14.4);

    public GarlicGarlandBlock() {
        super(Properties.of().mapColor(MapColor.PLANT).sound(SoundType.GRASS).strength(0.2F).noCollission()
                .pushReaction(PushReaction.DESTROY));
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            default -> NORTH;
        };
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !repels(entity)) return;
        Vec3 outward = entity.position().subtract(Vec3.atCenterOf(pos));
        double distanceSquared = outward.lengthSqr();
        if (distanceSquared == 0 || distanceSquared > 36) return;

        double vertical = outward.y * 4.32 / (distanceSquared * distanceSquared);
        if (vertical > 0.2 || vertical < -0.1) vertical = 0.12;
        entity.push(Math.signum(outward.x) * 0.22, vertical, Math.signum(outward.z) * 0.22);
        entity.hurtMarked = true;
    }

    private static boolean repels(Entity entity) {
        return entity instanceof Vampire || entity instanceof Player player
                && !player.getAbilities().instabuild && !player.isSpectator() && Vampirism.level(player) > 0;
    }
}
