package art.arcane.witchery.block;

import art.arcane.witchery.entity.AnimalMerchant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CritterSnareBlock extends Block {
    public static final EnumProperty<Critter> CRITTER = EnumProperty.create("critter", Critter.class);
    public static final BooleanProperty STOCKED = BooleanProperty.create("stocked");
    private static final VoxelShape SHAPE = box(0.8, 0, 0.8, 15.2, 16, 15.2);

    public CritterSnareBlock() {
        super(Properties.copy(Blocks.DANDELION).noCollission());
        registerDefaultState(defaultBlockState().setValue(CRITTER, Critter.EMPTY).setValue(STOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CRITTER, STOCKED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (side == Direction.DOWN && !canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, side, neighbor, level, pos, neighborPos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel server) || !entity.isAlive() || state.getValue(CRITTER) != Critter.EMPTY) return;
        Critter critter = Critter.forEntity(entity);
        if (critter == Critter.EMPTY) return;
        boolean stocked = critter == Critter.BAT && entity.getPersistentData().contains(AnimalMerchant.STOCK_TAG);
        if (server.setBlockAndUpdate(pos, state.setValue(CRITTER, critter).setValue(STOCKED, stocked))) entity.discard();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        Critter critter = state.getValue(CRITTER);
        if (level instanceof ServerLevel server && critter != Critter.EMPTY && player.isShiftKeyDown()) {
            Mob mob = critter.type.create(server);
            if (mob == null) return InteractionResult.FAIL;
            if (mob instanceof Slime slime) slime.setSize(1, true);
            if (state.getValue(STOCKED) && critter == Critter.BAT) {
                mob.getPersistentData().put(AnimalMerchant.STOCK_TAG, new CompoundTag());
            }
            if (critter == Critter.BAT) {
                mob.moveTo(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 0, 0);
            } else {
                mob.moveTo(pos.getX() + (player.getX() < pos.getX() ? -0.5 : 1.5), player.getY() + 0.5,
                        pos.getZ() + (player.getZ() < pos.getZ() ? -0.5 : 1.5), 0, 0);
            }
            if (server.addFreshEntity(mob)) level.setBlockAndUpdate(pos, defaultBlockState());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Critter critter = state.getValue(CRITTER);
        if (critter != Critter.EMPTY && random.nextInt(24) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, critter.sound, SoundSource.NEUTRAL,
                    0.5F, 0.4F / (0.8F + random.nextFloat() * 0.4F), false);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack snare = new ItemStack(this);
        writeCritter(snare, state.getValue(CRITTER), state.getValue(STOCKED));
        return snare;
    }

    public static void applyLegacyMetadata(ItemStack stack, int damage) {
        int index = damage & 7;
        Critter[] critters = Critter.values();
        if (index > 0 && index < critters.length) writeCritter(stack, critters[index], (damage & 8) != 0);
    }

    public static Critter critterFromStack(ItemStack stack) {
        CompoundTag state = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return state == null ? Critter.EMPTY : CRITTER.getValue(state.getString(CRITTER.getName())).orElse(Critter.EMPTY);
    }

    private static void writeCritter(ItemStack stack, Critter critter, boolean stocked) {
        if (critter == Critter.EMPTY) return;
        CompoundTag state = stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG);
        state.putString(CRITTER.getName(), critter.getSerializedName());
        state.putString(STOCKED.getName(), Boolean.toString(stocked));
    }

    public enum Critter implements StringRepresentable {
        EMPTY("empty", null, null),
        BAT("bat", EntityType.BAT, SoundEvents.BAT_AMBIENT),
        SILVERFISH("silverfish", EntityType.SILVERFISH, SoundEvents.SILVERFISH_AMBIENT),
        SLIME("slime", EntityType.SLIME, SoundEvents.SLIME_SQUISH_SMALL),
        MAGMA_CUBE("magmacube", EntityType.MAGMA_CUBE, SoundEvents.MAGMA_CUBE_SQUISH_SMALL);

        private final String name;
        private final EntityType<? extends Mob> type;
        private final SoundEvent sound;

        Critter(String name, EntityType<? extends Mob> type, SoundEvent sound) {
            this.name = name;
            this.type = type;
            this.sound = sound;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        private static Critter forEntity(Entity entity) {
            if (entity instanceof Slime slime && !slime.isTiny()) return EMPTY;
            for (Critter critter : values()) {
                if (entity.getType() == critter.type) return critter;
            }
            return EMPTY;
        }
    }
}
