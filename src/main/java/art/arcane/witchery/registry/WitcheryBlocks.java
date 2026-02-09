package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryBlocks {
    private static final VoxelShape SHAPE_FULL_BLOCK = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_FLOWER_SMALL = Block.box(4.8D, 0.0D, 4.8D, 11.2D, 9.6D, 11.2D);
    private static final VoxelShape SHAPE_FLOWER_TALL = Block.box(0.8D, 0.0D, 0.8D, 15.2D, 16.0D, 15.2D);
    private static final VoxelShape SHAPE_FLOWER_LOW = Block.box(1.6D, 0.0D, 1.6D, 14.4D, 6.4D, 14.4D);
    private static final VoxelShape SHAPE_PAD_FLAT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.5D, 16.0D);
    private static final VoxelShape SHAPE_GRASSPER = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.16D, 16.0D);
    private static final VoxelShape SHAPE_ALTAR = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
    private static final VoxelShape SHAPE_KETTLE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    private static final VoxelShape SHAPE_MACHINE_COMPACT = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
    private static final VoxelShape SHAPE_FUNNEL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_COFFIN = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);
    private static final VoxelShape SHAPE_STATUE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 22.0D, 14.0D);
    private static final VoxelShape SHAPE_CHEST = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    private static final VoxelShape SHAPE_WOLFTRAP = Block.box(3.0D, 0.0D, 1.0D, 13.0D, 2.01D, 15.0D);
    private static final VoxelShape SHAPE_BEARTRAP = Block.box(3.2D, 0.16D, 3.2D, 12.8D, 1.6D, 12.8D);
    private static final VoxelShape SHAPE_DEMONHEART = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 12.8D, 12.0D);
    private static final VoxelShape SHAPE_SKULL = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);
    private static final VoxelShape SHAPE_CHALICE = Block.box(4.8D, 0.0D, 5.92D, 10.08D, 7.36D, 11.12D);
    private static final VoxelShape SHAPE_CANDELABRA = Block.box(1.6D, 0.0D, 1.6D, 14.4D, 16.0D, 14.4D);
    private static final VoxelShape SHAPE_BRAZIER = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 15.2D, 12.8D);
    private static final VoxelShape SHAPE_CRYSTAL_BALL = Block.box(4.8D, 0.0D, 4.8D, 11.2D, 9.6D, 11.2D);
    private static final VoxelShape SHAPE_AREA_MARKER = Block.box(2.4D, 0.0D, 2.4D, 13.6D, 8.0D, 13.6D);
    private static final VoxelShape SHAPE_FETISH = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 16.0D, 12.8D);
    private static final VoxelShape SHAPE_PLACED_ITEM = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 0.8D, 12.8D);
    private static final VoxelShape SHAPE_GLOW_GLOBE = Block.box(6.4D, 6.4D, 6.4D, 9.6D, 9.6D, 9.6D);
    private static final VoxelShape SHAPE_INFINITY_EGG = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_GARLIC_GARLAND = Block.box(1.6D, 12.8D, 13.6D, 14.4D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_POPPET_SHELF = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.16D, 16.0D);
    private static final VoxelShape SHAPE_DREAM_CATCHER = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
    private static final VoxelShape SHAPE_SILVER_VAT = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.24D, 16.0D);
    private static final VoxelShape SHAPE_DAYLIGHT_COLLECTOR = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 12.8D, 12.8D);
    private static final VoxelShape SHAPE_BLOOD_CRUCIBLE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 4.96D, 12.0D);
    private static final VoxelShape SHAPE_MIRROR_PANEL = Block.box(0.0D, 0.0D, 13.6D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_GLYPH = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.25D, 16.0D);
    private static final VoxelShape SHAPE_PORTAL_CROSS = Shapes.or(
            Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D),
            Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D)
    );
    private static final VoxelShape SHAPE_CACTUS = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final VoxelShape SHAPE_VOID_BRAMBLE = Block.box(0.8D, 0.0D, 0.8D, 15.2D, 16.0D, 15.2D);
    private static final VoxelShape SHAPE_CAULDRON = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 13.6D, 16.0D);
    private static final VoxelShape SHAPE_WOLF_ALTAR = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 16.0D);
    private static final VoxelShape SHAPE_STATUE_GODDESS = Block.box(0.0D, 0.0D, 1.6D, 16.0D, 32.0D, 14.4D);

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
            case "witchsapling" -> new LegacyWitchSaplingVariantBlock();
            case "wickerbundle" -> new LegacyWickerBundleBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));
            case "shadedglass" -> new LegacyShadedGlassBlock(false);
            case "shadedglass_active" -> new LegacyShadedGlassBlock(true);
            case "bloodrose" -> new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_FLOWER_SMALL, false);
            case "plantmine" -> new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_FLOWER_SMALL, true);
            case "glintweed", "crittersnare", "bramble", "somniancotton", "spanishmoss", "vine", "web" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_FLOWER_TALL, false);
            case "embermoss" -> new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_FLOWER_LOW, false);
            case "lilypad", "leapinglily" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.LILY_PAD), SHAPE_PAD_FLAT, false);
            case "grassper" -> new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_GRASSPER, false);
            case "icedoor" -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).noOcclusion(), BlockSetType.IRON);
            case "rowanwooddoor", "alderwooddoor", "cwoodendoor" ->
                    new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR).noOcclusion(), BlockSetType.OAK);
            case "altar" -> new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_ALTAR, true);
            case "kettle" -> new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_KETTLE, true);
            case "spinningwheel", "distilleryidle", "distilleryburning", "witchesovenidle", "witchesovenburning" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_MACHINE_COMPACT, true);
            case "fumefunnel", "filteredfumefunnel" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_FUNNEL, true);
            case "coffinblock" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_COFFIN, true);
            case "statueofworship" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_STATUE, true);
            case "refillingchest", "leechchest" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.CHEST).noOcclusion(), SHAPE_CHEST, true);
            case "stairswoodrowan", "stairswoodalder", "stairswoodhawthorn" ->
                    new StairBlock(Blocks.OAK_PLANKS.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS));
            case "icestairs", "snowstairs" ->
                    new StairBlock(Blocks.STONE.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.STONE_STAIRS));
            case "witchwoodslab", "iceslab", "snowslab" ->
                    new SlabBlock(BlockBehaviour.Properties.copy(Blocks.STONE_SLAB));
            case "icefence", "stockade", "icestockade" ->
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
            case "wolftrap" -> new LegacyWolftrapBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion().noCollission());
            case "beartrap" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.METAL), SHAPE_BEARTRAP, false);
            case "demonheart" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.STONE), SHAPE_DEMONHEART, true);
            case "wolfhead", "alluringskull" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL), SHAPE_SKULL, true);
            case "chalice" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).sound(SoundType.METAL), SHAPE_CHALICE, true);
            case "candelabra" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).sound(SoundType.METAL), SHAPE_CANDELABRA, true);
            case "brazier" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.METAL), SHAPE_BRAZIER, true);
            case "crystalball" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.GLASS), SHAPE_CRYSTAL_BALL, true);
            case "decurseteleport", "decursedirected" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.STONE), SHAPE_AREA_MARKER, true);
            case "scarecrow", "trent", "witchsladder" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), SHAPE_FETISH, true);
            case "placeditem" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), SHAPE_PLACED_ITEM, true);
            case "glowglobe" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.GLASS), SHAPE_GLOW_GLOBE, true);
            case "infinityegg" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DRAGON_EGG), SHAPE_INFINITY_EGG, true);
            case "garlicgarland" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES), SHAPE_GARLIC_GARLAND, false);
            case "poppetshelf" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), SHAPE_POPPET_SHELF, true);
            case "dreamcatcher" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR), SHAPE_DREAM_CATCHER, false);
            case "silvervat" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.METAL), SHAPE_SILVER_VAT, true);
            case "daylightcollector" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DAYLIGHT_DETECTOR), SHAPE_DAYLIGHT_COLLECTOR, true);
            case "bloodcrucible" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.STONE), SHAPE_BLOOD_CRUCIBLE, true);
            case "mirrorblock", "mirrorblock2" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.GLASS), SHAPE_MIRROR_PANEL, true);
            case "circle", "circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_CARPET), SHAPE_GLYPH, false);
            case "spiritportal", "tormentportal", "mirrorwall" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL), SHAPE_PORTAL_CROSS, false);
            case "spiritflowing" -> createLegacyFluidBlock("fluidspirit");
            case "hollowtears" -> createLegacyFluidBlock("hollowtears");
            case "disease" -> createLegacyFluidBlock("fluiddisease");
            case "brew" -> createLegacyFluidBlock("brew");
            case "brewgas" -> createLegacyFluidBlock("brewgas");
            case "brewliquid" -> createLegacyFluidBlock("brewliquid");
            case "force", "barrier" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER), SHAPE_FULL_BLOCK, true);
            case "light", "slurp" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noLootTable(), Shapes.empty(), false);
            case "cactus" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.CACTUS), SHAPE_CACTUS, true);
            case "voidbramble" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), SHAPE_VOID_BRAMBLE, false);
            case "cauldron" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON), SHAPE_CAULDRON, true);
            case "wolfaltar" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.STONE), SHAPE_WOLF_ALTAR, true);
            case "statuegoddess" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.STONE), SHAPE_STATUE_GODDESS, true);
            case "pitdirt" -> new Block(BlockBehaviour.Properties.copy(Blocks.DIRT).noOcclusion());
            case "pitgrass" -> new Block(BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).noOcclusion());
            case "wallgen" -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 3600000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .noCollission());
            default -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
        };
    }

    private static LiquidBlock createLegacyFluidBlock(String legacyFluidName) {
        return new LiquidBlock(WitcheryFluids.getRequiredSource(legacyFluidName),
                BlockBehaviour.Properties.copy(Blocks.WATER)
                        .strength(100.0F)
                        .noCollission()
                        .noLootTable());
    }

    private static final class LegacyNonSolidShapeBlock extends Block {
        private final VoxelShape shape;
        private final boolean hasCollision;

        private LegacyNonSolidShapeBlock(BlockBehaviour.Properties properties, VoxelShape shape, boolean hasCollision) {
            super(hasCollision ? properties.noOcclusion() : properties.noOcclusion().noCollission());
            this.shape = shape;
            this.hasCollision = hasCollision;
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return shape;
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return hasCollision ? shape : Shapes.empty();
        }

        @Override
        public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
            return Shapes.empty();
        }
    }

    private static final class LegacyHorizontalFacingBlock extends HorizontalDirectionalBlock {
        private final VoxelShape shape;
        private final boolean hasCollision;

        private LegacyHorizontalFacingBlock(BlockBehaviour.Properties properties) {
            this(properties, null, true);
        }

        private LegacyHorizontalFacingBlock(BlockBehaviour.Properties properties, VoxelShape shape, boolean hasCollision) {
            super(properties);
            this.shape = shape;
            this.hasCollision = hasCollision;
            registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext context) {
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return shape == null ? super.getShape(state, level, pos, context) : shape;
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            if (shape == null) {
                return super.getCollisionShape(state, level, pos, context);
            }
            return hasCollision ? shape : Shapes.empty();
        }

        @Override
        public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
            return shape == null ? super.getOcclusionShape(state, level, pos) : Shapes.empty();
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

    private static final class LegacyWitchSaplingVariantBlock extends Block {
        private static final EnumProperty<LegacyWitchWoodType> WOOD_TYPE =
                EnumProperty.create("wood_type", LegacyWitchWoodType.class);

        private LegacyWitchSaplingVariantBlock() {
            super(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).noOcclusion().noCollission());
            registerDefaultState(defaultBlockState().setValue(WOOD_TYPE, LegacyWitchWoodType.ROWAN));
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE_FLOWER_TALL;
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return Shapes.empty();
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
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE_WOLFTRAP;
        }

        @Override
        public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return Shapes.empty();
        }

        @Override
        public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
            return Shapes.empty();
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(FACING);
        }
    }
}
