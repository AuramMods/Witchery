package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Witchery.MODID);
    public static final Map<String, RegistryObject<Block>> LEGACY_BLOCKS = new LinkedHashMap<>();

    static {
        for (String legacyName : LegacyRegistryData.BLOCKS) {
            registerLegacyBlock(legacyName);
        }
    }

    private WitcheryBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    public static RegistryObject<Block> getRequired(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        RegistryObject<Block> block = LEGACY_BLOCKS.get(path);
        if (block == null) {
            throw new IllegalStateException("Missing legacy block placeholder for path: " + path);
        }
        return block;
    }

    public static Collection<RegistryObject<Block>> all() {
        return Collections.unmodifiableCollection(LEGACY_BLOCKS.values());
    }

    private static void registerLegacyBlock(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_BLOCKS.containsKey(path)) {
            return;
        }

        RegistryObject<Block> block = BLOCKS.register(path, () -> createBlock(path));
        LEGACY_BLOCKS.put(path, block);
    }

    private static Block createBlock(String path) {
        return switch (path) {
            case "witchlog" -> new LegacyWitchLogBlock();
            case "witchwood" -> new LegacyWitchWoodVariantBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));
            case "witchleaves" -> new LegacyWitchWoodVariantBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).noOcclusion());
            case "witchsapling" -> new LegacyWitchWoodVariantBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).noOcclusion());
            case "wickerbundle" -> new LegacyWickerBundleBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));
            case "shadedglass" -> new LegacyShadedGlassBlock(false);
            case "shadedglass_active" -> new LegacyShadedGlassBlock(true);
            case "icedoor" -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).noOcclusion(), BlockSetType.IRON);
            case "rowanwooddoor", "alderwooddoor", "cwoodendoor" ->
                    new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR).noOcclusion(), BlockSetType.OAK);
            case "altar", "kettle", "spinningwheel", "distilleryidle", "distilleryburning",
                    "witchesovenidle", "witchesovenburning", "fumefunnel", "filteredfumefunnel",
                    "coffinblock", "statueofworship", "refillingchest", "leechchest" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion());
            case "stairswoodrowan", "stairswoodalder", "stairswoodhawthorn" ->
                    new StairBlock(Blocks.OAK_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS));
            case "icestairs", "snowstairs" ->
                    new StairBlock(Blocks.STONE.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.STONE_STAIRS));
            case "witchwoodslab", "iceslab", "snowslab" ->
                    new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_SLAB));
            case "icefence" ->
                    new FenceBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE));
            case "icefencegate" ->
                    new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE), WoodType.OAK);
            case "icepressureplate", "snowpressureplate", "cwoodpressureplate", "csnowpressureplate" ->
                    new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(Blocks.OAK_PRESSURE_PLATE), BlockSetType.OAK);
            case "cstonepressureplate" ->
                    new PressurePlateBlock(PressurePlateBlock.Sensitivity.MOBS, BlockBehaviour.Properties.copy(Blocks.STONE_PRESSURE_PLATE), BlockSetType.STONE);
            case "cbuttonwood" ->
                    new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON), BlockSetType.OAK, 30, true);
            case "cbuttonstone" ->
                    new ButtonBlock(BlockBehaviour.Properties.copy(Blocks.STONE_BUTTON), BlockSetType.STONE, 20, false);
            case "belladonna", "mandrake", "artichoke", "snowbell", "wormwood", "mindrake" ->
                    new LegacyCropBlock(4);
            case "garlicplant" -> new LegacyCropBlock(5);
            case "wolfsbane" -> new LegacyCropBlock(7);
            case "wolftrap" -> new LegacyWolftrapBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion());
            case "wallgen" -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .noCollission());
            default -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
        };
    }

    private static final class LegacyHorizontalFacingBlock extends HorizontalDirectionalBlock {
        private LegacyHorizontalFacingBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }
    }

    private static final class LegacyWitchLogBlock extends RotatedPillarBlock {
        private static final EnumProperty<LegacyWitchWoodType> WOOD_TYPE =
                EnumProperty.create("wood_type", LegacyWitchWoodType.class);

        private LegacyWitchLogBlock() {
            super(BlockBehaviour.Properties.copy(Blocks.OAK_LOG));
            registerDefaultState(defaultBlockState()
                    .setValue(AXIS, Direction.Axis.Y)
                    .setValue(WOOD_TYPE, LegacyWitchWoodType.ROWAN));
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState()
                    .setValue(AXIS, context.getClickedFace().getAxis())
                    .setValue(WOOD_TYPE, LegacyWitchWoodType.ROWAN);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(AXIS, WOOD_TYPE);
        }
    }

    private static final class LegacyWitchWoodVariantBlock extends Block {
        private static final EnumProperty<LegacyWitchWoodType> WOOD_TYPE =
                EnumProperty.create("wood_type", LegacyWitchWoodType.class);

        private LegacyWitchWoodVariantBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(WOOD_TYPE, LegacyWitchWoodType.ROWAN));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(WOOD_TYPE);
        }
    }

    private static final class LegacyWickerBundleBlock extends Block {
        private static final EnumProperty<LegacyWickerBundleType> BUNDLE_TYPE =
                EnumProperty.create("bundle_type", LegacyWickerBundleType.class);

        private LegacyWickerBundleBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(BUNDLE_TYPE, LegacyWickerBundleType.PLAIN));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(BUNDLE_TYPE);
        }
    }

    private static final class LegacyShadedGlassBlock extends Block {
        private static final EnumProperty<LegacyShadedGlassColor> COLOR =
                EnumProperty.create("color", LegacyShadedGlassColor.class);

        private LegacyShadedGlassBlock(boolean active) {
            super(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .lightLevel(state -> active ? 15 : 0)
                    .noOcclusion());
            registerDefaultState(defaultBlockState().setValue(COLOR, LegacyShadedGlassColor.WHITE));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(COLOR);
        }
    }

    private enum LegacyWitchWoodType implements StringRepresentable {
        ROWAN("rowan"),
        ALDER("alder"),
        HAWTHORN("hawthorn");

        private final String serializedName;

        LegacyWitchWoodType(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    private enum LegacyWickerBundleType implements StringRepresentable {
        PLAIN("plain"),
        BLOODIED("bloodied");

        private final String serializedName;

        LegacyWickerBundleType(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    private enum LegacyShadedGlassColor implements StringRepresentable {
        WHITE("white"),
        ORANGE("orange"),
        MAGENTA("magenta"),
        LIGHT_BLUE("light_blue"),
        YELLOW("yellow"),
        LIME("lime"),
        PINK("pink"),
        GRAY("gray"),
        SILVER("silver"),
        CYAN("cyan"),
        PURPLE("purple"),
        BLUE("blue"),
        BROWN("brown"),
        GREEN("green"),
        RED("red"),
        BLACK("black");

        private final String serializedName;

        LegacyShadedGlassColor(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }

    private static final class LegacyCropBlock extends CropBlock {
        private final int maxAge;

        private LegacyCropBlock(int maxAge) {
            super(BlockBehaviour.Properties.copy(Blocks.WHEAT));
            this.maxAge = maxAge;
        }

        @Override
        protected Item getBaseSeedId() {
            return net.minecraft.world.item.Items.WHEAT_SEEDS;
        }

        @Override
        public int getMaxAge() {
            return maxAge;
        }
    }

    private static final class LegacyWolftrapBlock extends HorizontalDirectionalBlock {
        private LegacyWolftrapBlock(BlockBehaviour.Properties properties) {
            super(properties.sound(SoundType.METAL));
            registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }
    }
}
