package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.CursedControlBlockEntity;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class CursedControls {
    private CursedControls() {}

    public enum Kind {
        STONE_BUTTON("cbuttonstone", () -> Blocks.STONE_BUTTON, 20),
        WOODEN_BUTTON("cbuttonwood", () -> Blocks.OAK_BUTTON, 30),
        LEVER("clever", () -> Blocks.LEVER, 0),
        WOODEN_PLATE("cwoodpressureplate", () -> Blocks.OAK_PRESSURE_PLATE, 20),
        STONE_PLATE("cstonepressureplate", () -> Blocks.STONE_PRESSURE_PLATE, 20),
        SNOW_PLATE("csnowpressureplate", () -> WitcheryBlocks.getRequired("snowpressureplate").get(), 20),
        WOODEN_DOOR("cwoodendoor", () -> Blocks.OAK_DOOR, 0);

        private final String path;
        private final Supplier<Block> original;
        private final int pressTicks;

        Kind(String path, Supplier<Block> original, int pressTicks) {
            this.path = path;
            this.original = original;
            this.pressTicks = pressTicks;
        }

        public String path() { return path; }
        public Block original() { return original.get(); }
        public Block cursed() { return WitcheryBlocks.getRequired(path).get(); }
    }

    public interface Control extends EntityBlock {
        Kind kind();

        @Override
        default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new CursedControlBlockEntity(WitcheryBlockEntities.cursedControl(kind()).get(), pos, state);
        }
    }

    public static void impact(ServerLevel level, HitResult hit, CompoundTag brew, BrewMixture mixture, int color,
                              @Nullable Entity thrower) {
        if (hit instanceof BlockHitResult block) {
            Direction face = block.getDirection();
            BlockPos direct = block.getBlockPos();
            if (install(level, direct.relative(face), face, brew, mixture, color, thrower)) return;
            install(level, direct, face, brew, mixture, color, thrower);
        } else if (hit instanceof EntityHitResult entity) {
            install(level, entity.getEntity().blockPosition(), Direction.UP, brew, mixture, color, thrower);
        }
    }

    private static boolean install(ServerLevel level, BlockPos pos, Direction face, CompoundTag brew, BrewMixture mixture,
                                   int color, @Nullable Entity thrower) {
        BlockState state = level.getBlockState(pos);
        Kind kind = kindOf(state);
        if (kind == null) return false;
        boolean door = kind == Kind.WOODEN_DOOR;
        BlockPos base = door ? lowerDoor(pos, state) : pos;
        if (!canChange(level, base, thrower) || door && !canChange(level, base.above(), thrower)) {
            return true;
        }
        if (!(state.getBlock() instanceof Control)) {
            Block changed = kind.cursed();
            if (door) {
                if (!swapDoor(level, base, kind.original(), changed, thrower, face)) return true;
            } else {
                BlockState matching = changed.withPropertiesOf(state);
                BlockState replacement = kind == Kind.STONE_BUTTON || kind == Kind.WOODEN_BUTTON
                        ? matching.setValue(BlockStateProperties.POWERED, false) : matching;
                // A powered control's normal removal notifies its support even while Forge captures snapshots.
                int flags = Block.UPDATE_ALL | Block.UPDATE_MOVE_BY_PISTON;
                if (!WitcheryBlockPlacement.placeTogether(level, thrower, face,
                        () -> level.setBlock(pos, replacement, flags))) return true;
                state.onRemove(level, pos, replacement, false);
                scheduleRelease(level, pos, replacement, kind);
            }
        }
        if (level.getBlockEntity(base) instanceof CursedControlBlockEntity control) {
            control.charge(brew, mixture, color, thrower);
        }
        return true;
    }

    @Nullable
    private static Kind kindOf(BlockState state) {
        if (state.getBlock() instanceof Control control) return control.kind();
        for (Kind kind : Kind.values()) {
            if (state.is(kind.original())) return kind;
        }
        return null;
    }

    private static boolean canChange(ServerLevel level, BlockPos pos, @Nullable Entity thrower) {
        return level.isInWorldBounds(pos) && level.getWorldBorder().isWithinBounds(pos)
                && (!(thrower instanceof Player player) || level.mayInteract(player, pos)
                && !WitcheryBlockBreak.isCanceled(level, pos, level.getBlockState(pos), player));
    }

    private static void activated(Level level, BlockPos pos, Entity target, Kind kind) {
        if (!(level instanceof ServerLevel server)) return;
        BlockState state = server.getBlockState(pos);
        if (!state.is(kind.cursed())) return;
        boolean door = kind == Kind.WOODEN_DOOR;
        BlockPos base = door ? lowerDoor(pos, state) : pos;
        if (!(server.getBlockEntity(base) instanceof CursedControlBlockEntity control) || !control.spend(server, target)) {
            return;
        }
        if (door) {
            swapDoor(server, base, kind.cursed(), kind.original(), null, Direction.UP);
        } else {
            BlockState restored = kind.original().withPropertiesOf(state);
            server.setBlockAndUpdate(pos, restored);
            scheduleRelease(server, pos, restored, kind);
        }
    }

    private static void scheduleRelease(ServerLevel level, BlockPos pos, BlockState state, Kind kind) {
        if (kind.pressTicks > 0 && state.getValue(BlockStateProperties.POWERED)) {
            level.scheduleTick(pos, state.getBlock(), kind.pressTicks);
        }
    }

    private static BlockPos lowerDoor(BlockPos pos, BlockState state) {
        return state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    private static boolean swapDoor(ServerLevel level, BlockPos pos, Block from, Block to,
                                    @Nullable Entity thrower, Direction face) {
        BlockPos above = pos.above();
        BlockState bottom = level.getBlockState(pos);
        BlockState top = level.getBlockState(above);
        if (!bottom.is(from) || !top.is(from)
                || bottom.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER
                || top.getValue(DoorBlock.HALF) != DoubleBlockHalf.UPPER) return false;
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        if (!WitcheryBlockPlacement.placeTogether(level, thrower, face, () ->
                level.setBlock(pos, to.withPropertiesOf(bottom), flags)
                        && level.setBlock(above, to.withPropertiesOf(top), flags))) return false;
        level.updateNeighborsAt(pos, to);
        level.updateNeighborsAt(above, to);
        return true;
    }

    public static final class Button extends ButtonBlock implements Control {
        private final Kind kind;

        public Button(boolean wooden) {
            super(Properties.copy(wooden ? Blocks.OAK_BUTTON : Blocks.STONE_BUTTON), wooden ? BlockSetType.OAK : BlockSetType.STONE,
                    wooden ? Kind.WOODEN_BUTTON.pressTicks : Kind.STONE_BUTTON.pressTicks, wooden);
            kind = wooden ? Kind.WOODEN_BUTTON : Kind.STONE_BUTTON;
        }

        @Override
        public Kind kind() { return kind; }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                     InteractionHand hand, BlockHitResult hit) {
            InteractionResult result = super.use(state, level, pos, player, hand, hit);
            if (!state.getValue(POWERED) && result.consumesAction()) activated(level, pos, player, kind);
            return result;
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return new ItemStack(kind.original());
        }
    }

    public static final class Lever extends LeverBlock implements Control {
        public Lever() { super(Properties.copy(Blocks.LEVER)); }

        @Override
        public Kind kind() { return Kind.LEVER; }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                     InteractionHand hand, BlockHitResult hit) {
            InteractionResult result = super.use(state, level, pos, player, hand, hit);
            if (result.consumesAction()) activated(level, pos, player, kind());
            return result;
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return new ItemStack(kind().original());
        }
    }

    public static final class Plate extends PressurePlateBlock implements Control {
        private final Kind kind;

        public Plate(Kind kind) {
            super(kind == Kind.STONE_PLATE ? Sensitivity.MOBS : Sensitivity.EVERYTHING,
                    switch (kind) {
                        case STONE_PLATE -> Properties.copy(Blocks.STONE_PRESSURE_PLATE);
                        case SNOW_PLATE -> Properties.copy(Blocks.OAK_PRESSURE_PLATE).requiresCorrectToolForDrops();
                        default -> Properties.copy(Blocks.OAK_PRESSURE_PLATE);
                    },
                    kind == Kind.STONE_PLATE ? BlockSetType.STONE : BlockSetType.OAK);
            this.kind = kind;
        }

        @Override
        public Kind kind() { return kind; }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            super.entityInside(state, level, pos, entity);
            BlockState current = level.getBlockState(pos);
            if (!state.getValue(POWERED) && current.is(this) && current.getValue(POWERED)) {
                activated(level, pos, entity, kind);
            }
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return new ItemStack(kind.original());
        }
    }

    public static final class Door extends DoorBlock implements Control {
        public Door() { super(Properties.copy(Blocks.OAK_DOOR).noOcclusion(), BlockSetType.OAK); }

        @Override
        public Kind kind() { return Kind.WOODEN_DOOR; }

        @Override
        public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                     InteractionHand hand, BlockHitResult hit) {
            InteractionResult result = super.use(state, level, pos, player, hand, hit);
            if (result.consumesAction()) activated(level, pos, player, kind());
            return result;
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return new ItemStack(kind().original());
        }
    }
}
