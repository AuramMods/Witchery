package art.arcane.witchery.block;

import art.arcane.witchery.brewing.BrewImpacts;
import art.arcane.witchery.brewing.BrewTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public final class PlantMineBlock extends Block {
    public static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);
    private static final VoxelShape SHAPE = box(5.8, 0, 5.8, 10.2, 8.6, 10.2);

    public PlantMineBlock() {
        super(Properties.of().mapColor(MapColor.PLANT).sound(SoundType.GRASS).strength(6, 600)
                .noOcclusion().randomTicks().noLootTable().pushReaction(PushReaction.DESTROY));
        registerDefaultState(defaultBlockState().setValue(VARIANT, Variant.ROSE_WEBS));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState soil = level.getBlockState(pos.below());
        return (level.getMaxLocalRawBrightness(pos) >= 8 || level.canSeeSky(pos))
                && (soil.is(BlockTags.DIRT) || soil.is(BlockTags.SAND) || soil.is(Blocks.FARMLAND));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) level.removeBlock(pos, false);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel server) || !level.removeBlock(pos, false)) return;
        server.sendParticles(ParticleTypes.ENCHANTED_HIT, pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5,
                16, 0.25, 0.4, 0.25, 0.08);
        server.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.5F, 1);
        switch (state.getValue(VARIANT).effect) {
            case WEBS -> BrewTerrain.mineWebs(server, pos);
            case INK -> BrewImpacts.mineInk(server, pos, entity);
            case THORNS -> BrewTerrain.mineThorns(server, pos);
            case SPROUTING -> BrewTerrain.mineSprouting(server, pos);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(VARIANT).stack((Item) asItem());
    }

    private enum Effect { WEBS, INK, THORNS, SPROUTING }
    private enum Plant { ROSE, DANDELION, GRASS }

    public enum Variant implements StringRepresentable {
        ROSE_WEBS(Plant.ROSE, Effect.WEBS),
        ROSE_INK(Plant.ROSE, Effect.INK),
        ROSE_THORNS(Plant.ROSE, Effect.THORNS),
        ROSE_SPROUTING(Plant.ROSE, Effect.SPROUTING),
        DANDELION_WEBS(Plant.DANDELION, Effect.WEBS),
        DANDELION_INK(Plant.DANDELION, Effect.INK),
        DANDELION_THORNS(Plant.DANDELION, Effect.THORNS),
        DANDELION_SPROUTING(Plant.DANDELION, Effect.SPROUTING),
        GRASS_WEBS(Plant.GRASS, Effect.WEBS),
        GRASS_INK(Plant.GRASS, Effect.INK),
        GRASS_THORNS(Plant.GRASS, Effect.THORNS),
        GRASS_SPROUTING(Plant.GRASS, Effect.SPROUTING);

        private static final Variant[] BY_METADATA = values();
        private final String name;
        private final Plant plant;
        private final Effect effect;

        Variant(Plant plant, Effect effect) {
            this.name = name().toLowerCase(Locale.ROOT);
            this.plant = plant;
            this.effect = effect;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public Block tooltipPlant() {
            return switch (plant) {
                case ROSE -> Blocks.POPPY;
                case DANDELION -> Blocks.DANDELION;
                case GRASS -> Blocks.DEAD_BUSH;
            };
        }

        public ItemStack stack(Item item) {
            ItemStack stack = new ItemStack(item);
            write(stack);
            return stack;
        }

        private void write(ItemStack stack) {
            stack.getOrCreateTagElement("BlockStateTag").putString(VARIANT.getName(), name);
        }

        public static void applyLegacyMetadata(ItemStack stack, int metadata) {
            if (metadata >= 0 && metadata < BY_METADATA.length && fromTag(stack).isEmpty()) {
                fromLegacyMetadata(metadata).write(stack);
            }
        }

        public static Variant fromLegacyMetadata(int metadata) {
            return metadata >= 0 && metadata < BY_METADATA.length ? BY_METADATA[metadata] : ROSE_WEBS;
        }

        private static Optional<Variant> fromTag(ItemStack stack) {
            var tag = stack.getTagElement("BlockStateTag");
            return tag == null ? Optional.empty() : VARIANT.getValue(tag.getString(VARIANT.getName()));
        }

        public static Variant fromStack(ItemStack stack) {
            return fromTag(stack).orElse(ROSE_WEBS);
        }
    }

    public static final class Item extends BlockItem {
        public Item(Block block) {
            super(block, new Properties());
        }

        @Override
        public String getDescriptionId(ItemStack stack) {
            return getDescriptionId() + "." + Variant.fromStack(stack).getSerializedName();
        }

        @Nullable
        @Override
        protected BlockState getPlacementState(BlockPlaceContext context) {
            BlockState state = super.getPlacementState(context);
            return state == null ? null : state.setValue(VARIANT, Variant.fromStack(context.getItemInHand()));
        }

        public static int plantModel(ItemStack stack) {
            return Variant.fromStack(stack).plant.ordinal();
        }
    }
}
