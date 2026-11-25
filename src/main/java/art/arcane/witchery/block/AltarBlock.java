package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.menu.AltarMenu;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.IPlantable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class AltarBlock extends BaseEntityBlock {
    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    private static final int SIZE = 6;
    private static final List<Direction> ALTAR_NEIGHBORS = List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
    private static final Structure NO_STRUCTURE = new Structure(List.of(), null);

    public AltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FORMED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel server) AltarBlockEntity.form(server, pos);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        super.onRemove(state, level, pos, replacement, moving);
        if (!replacement.is(this) && level instanceof ServerLevel server) AltarBlockEntity.form(server, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos source, boolean moving) {
        if (source.equals(pos.above()) && state.getValue(FORMED) && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof AltarBlockEntity altar) {
            altar.refreshArtifacts(server);
        }
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(FORMED)) return;

        BlockPos plant = pos.offset(random.nextInt(32) - 15, random.nextInt(8) - 3, random.nextInt(32) - 15);
        Block block = level.getBlockState(plant).getBlock();
        if (block instanceof LeavesBlock || block instanceof IPlantable) {
            level.addParticle(WitcheryRegistries.ALTAR_POWER.get(), plant.getX() + 0.5, plant.getY() + 0.5, plant.getZ() + 0.5,
                    pos.getX() - plant.getX(), pos.getY() - plant.getY(), pos.getZ() - plant.getZ());
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, WitcheryBlockEntities.ALTAR.get(), AltarBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() || !state.getValue(FORMED)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof AltarBlockEntity altar) {
            AltarBlockEntity core = altar.resolveCore(server);
            if (core == null) {
                return InteractionResult.PASS;
            }
            core.inspect(server);
            player.openMenu(new SimpleMenuProvider((id, inventory, viewer) -> new AltarMenu(id, core, pos),
                    Component.translatable("block.witchery.altar")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static List<BlockPos> members(ServerLevel level, BlockPos origin) {
        Structure structure = findStructure(level, origin, false);
        return structure.core() == null ? List.of() : structure.members();
    }

    public static Structure changedStructure(ServerLevel level, BlockPos origin) {
        return findStructure(level, origin, true);
    }

    private static Structure findStructure(ServerLevel level, BlockPos origin, boolean includeUnformed) {
        boolean originIsAltar = level.getBlockState(origin).getBlock() instanceof AltarBlock;
        if (!originIsAltar && !includeUnformed) return NO_STRUCTURE;

        Set<BlockPos> found = new LinkedHashSet<>();
        ArrayDeque<BlockPos> pending = new ArrayDeque<>();
        if (originIsAltar) found.add(origin.immutable());
        pending.add(origin.immutable());
        boolean valid = true;
        while (!pending.isEmpty()) {
            int neighbors = 0;
            BlockPos pos = pending.remove();
            for (Direction direction : ALTAR_NEIGHBORS) {
                BlockPos next = pos.relative(direction);
                if (level.hasChunkAt(next) && level.getBlockState(next).getBlock() instanceof AltarBlock) {
                    if (found.add(next)) {
                        pending.add(next);
                    }
                    neighbors++;
                }
            }
            // After a break the removed position seeds traversal of its neighbors.
            if (originIsAltar || !pos.equals(origin)) valid &= neighbors >= 2 && neighbors <= 3;
            if (!includeUnformed && (!valid || found.size() > SIZE)) return NO_STRUCTURE;
        }
        BlockPos core = valid && found.size() == SIZE ? found.iterator().next() : null;
        return new Structure(List.copyOf(found), core);
    }

    public record Structure(List<BlockPos> members, @Nullable BlockPos core) {}
}
