package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BarrierBlock;
import art.arcane.witchery.block.PoppetShelfBlock;
import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.block.CandelabraBlock;
import art.arcane.witchery.block.ChaliceBlock;
import art.arcane.witchery.block.CoffinBlock;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.BloodRoseBlock;
import art.arcane.witchery.block.BloodCrucibleBlock;
import art.arcane.witchery.block.GarlicGarlandBlock;
import art.arcane.witchery.block.SilverVatBlock;
import art.arcane.witchery.block.CrystalBallBlock;
import art.arcane.witchery.block.DaylightCollectorBlock;
import art.arcane.witchery.block.DemonHeartBlock;
import art.arcane.witchery.block.DistilleryBlock;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.GrassperBlock;
import art.arcane.witchery.block.KettleBlock;
import art.arcane.witchery.block.LeechChestBlock;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.RefillingChestBlock;
import art.arcane.witchery.block.SomnianCottonBlock;
import art.arcane.witchery.block.SpinningWheelBlock;
import art.arcane.witchery.block.SpiritFluidBlock;
import art.arcane.witchery.block.SpiritPortalBlock;
import art.arcane.witchery.block.SunlightBlock;
import art.arcane.witchery.block.TormentPortalBlock;
import art.arcane.witchery.block.PlacedItemBlock;
import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.block.WitchCropBlock;
import art.arcane.witchery.block.WitchLeavesBlock;
import art.arcane.witchery.block.WitchSaplingBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.WitchesOvenBlock;
import art.arcane.witchery.block.WolfHeadBlock;
import art.arcane.witchery.entity.Ent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import org.jetbrains.annotations.Nullable;

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
    private static final VoxelShape SHAPE_FUNNEL = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_STATUE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 22.0D, 14.0D);
    private static final VoxelShape SHAPE_WOLFTRAP = Block.box(3.0D, 0.0D, 1.0D, 13.0D, 2.01D, 15.0D);
    private static final VoxelShape SHAPE_BEARTRAP = Block.box(3.2D, 0.16D, 3.2D, 12.8D, 1.6D, 12.8D);
    private static final VoxelShape SHAPE_SKULL = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D);
    private static final VoxelShape SHAPE_BRAZIER = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 15.2D, 12.8D);
    private static final VoxelShape SHAPE_AREA_MARKER = Block.box(2.4D, 0.0D, 2.4D, 13.6D, 8.0D, 13.6D);
    private static final VoxelShape SHAPE_FETISH = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 16.0D, 12.8D);
    private static final VoxelShape SHAPE_GLOW_GLOBE = Block.box(6.4D, 6.4D, 6.4D, 9.6D, 9.6D, 9.6D);
    private static final VoxelShape SHAPE_INFINITY_EGG = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE_CACTUS = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
    private static final VoxelShape SHAPE_VOID_BRAMBLE = Block.box(0.8D, 0.0D, 0.8D, 15.2D, 16.0D, 15.2D);
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
            case "witchleaves" -> new WitchLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES));
            case "witchsapling" -> new WitchSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING));
            case "wickerbundle" -> new LegacyWickerBundleBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS));
            case "shadedglass" -> new LegacyShadedGlassBlock(false);
            case "shadedglass_active" -> new LegacyShadedGlassBlock(true);
            case "bloodrose" -> new BloodRoseBlock();
            case "bloodedwool" -> new Block(BlockBehaviour.Properties.copy(Blocks.RED_WOOL));
            case "plantmine" -> new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_FLOWER_SMALL, true);
            case "glintweed", "spanishmoss", "vine", "web" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_FLOWER_TALL, false);
            case "bramble" -> new BrambleBlock();
            case "crittersnare" -> new CritterSnareBlock();
            case "somniancotton" -> new SomnianCottonBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION).randomTicks());
            case "embermoss" -> new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION), SHAPE_FLOWER_LOW, false);
            case "lilypad", "leapinglily" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.LILY_PAD), SHAPE_PAD_FLAT, false);
            case "grassper" -> new GrassperBlock();
            case "icedoor" -> new DoorBlock(BlockBehaviour.Properties.copy(Blocks.IRON_DOOR).noOcclusion(), BlockSetType.IRON);
            case "rowanwooddoor", "alderwooddoor", "cwoodendoor" ->
                    new DoorBlock(BlockBehaviour.Properties.copy(Blocks.OAK_DOOR).noOcclusion(), BlockSetType.OAK);
            case "altar" -> new AltarBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0F));
            case "kettle" -> new KettleBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion());
            case "spinningwheel" ->
                    new SpinningWheelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).noOcclusion());
            case "distilleryidle", "distilleryburning" ->
                    new DistilleryBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3.5F).noOcclusion()
                            .lightLevel(state -> state.getValue(DistilleryBlock.LIT) ? 6 : 0));
            case "witchesovenidle", "witchesovenburning" ->
                    new WitchesOvenBlock(BlockBehaviour.Properties.copy(Blocks.FURNACE)
                            .strength(3.5F)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(WitchesOvenBlock.LIT) ? 13 : 0));
            case "fumefunnel", "filteredfumefunnel" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_FUNNEL, true);
            case "coffinblock" -> new CoffinBlock();
            case "statueofworship" ->
                    new LegacyHorizontalFacingBlock(BlockBehaviour.Properties.copy(Blocks.STONE).noOcclusion(), SHAPE_STATUE, true);
            case "refillingchest" -> new RefillingChestBlock();
            case "leechchest" -> new LeechChestBlock();
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
            case "belladonna", "mandrake", "artichoke", "snowbell", "wormwood", "mindrake", "garlicplant", "wolfsbane" ->
                    new WitchCropBlock(WitchCrop.forBlock(path), BlockBehaviour.Properties.copy(Blocks.WHEAT));
            case "wolftrap" -> new LegacyWolftrapBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion().noCollission());
            case "beartrap" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.METAL), SHAPE_BEARTRAP, false);
            case "demonheart" ->
                    new DemonHeartBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(1).noOcclusion()
                            .sound(SoundType.NETHERRACK).lightLevel(state -> 3));
            case "wolfhead" -> new WolfHeadBlock();
            case "alluringskull" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.SKELETON_SKULL), SHAPE_SKULL, true);
            case "chalice" ->
                    new ChaliceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).noOcclusion().strength(3.0F));
            case "candelabra" ->
                    new CandelabraBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).noOcclusion().strength(2.0F).lightLevel(state -> 15));
            case "brazier" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).sound(SoundType.METAL), SHAPE_BRAZIER, true);
            case "crystalball" -> new CrystalBallBlock();
            case "decurseteleport", "decursedirected" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.STONE), SHAPE_AREA_MARKER, true);
            case "scarecrow", "trent", "witchsladder" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS), SHAPE_FETISH, true);
            case "placeditem" -> new PlacedItemBlock();
            case "glowglobe" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.GLASS), SHAPE_GLOW_GLOBE, true);
            case "infinityegg" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.DRAGON_EGG), SHAPE_INFINITY_EGG, true);
            case "garlicgarland" -> new GarlicGarlandBlock();
            case "poppetshelf" -> new PoppetShelfBlock();
            case "dreamcatcher" ->
                    new DreamCatcherBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(1).noCollission().noOcclusion());
            case "silvervat" -> new SilverVatBlock();
            case "daylightcollector" -> new DaylightCollectorBlock();
            case "bloodcrucible" -> new BloodCrucibleBlock();
            case "mirrorblock", "mirrorblock2" -> new MirrorBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .strength(path.equals("mirrorblock") ? 1 : -1, 9_999).noLootTable(), path.equals("mirrorblock2"));
            case "circle" -> new RiteHeartBlock();
            case "circleglyphritual" -> new RitualGlyphBlock("chalkritual");
            case "circleglyphotherwhere" -> new RitualGlyphBlock("chalkotherwhere");
            case "circleglyphinfernal" -> new RitualGlyphBlock("chalkinfernal");
            case "spiritportal" -> new SpiritPortalBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL).noLootTable());
            case "mirrorwall" -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .strength(-1, 9_999).noLootTable());
            case "tormentportal" -> new TormentPortalBlock();
            case "tormentstone" -> new Block(BlockBehaviour.Properties.copy(Blocks.MYCELIUM)
                    .strength(-1, 9_999).noLootTable().noOcclusion());
            case "spiritflowing" -> new SpiritFluidBlock(WitcheryFluids.getRequiredSource("fluidspirit"), spiritProperties(), true);
            case "hollowtears" -> new SpiritFluidBlock(WitcheryFluids.getRequiredSource("hollowtears"), spiritProperties(), false);
            case "disease" -> createLegacyFluidBlock("fluiddisease");
            case "brew" -> createLegacyFluidBlock("brew");
            case "brewgas" -> createLegacyFluidBlock("brewgas");
            case "brewliquid" -> createLegacyFluidBlock("brewliquid");
            case "force" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noLootTable(), SHAPE_FULL_BLOCK, true);
            case "barrier" -> new BarrierBlock();
            case "light" -> new SunlightBlock();
            case "slurp" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER).noLootTable(), Shapes.empty(), false);
            case "cactus" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.CACTUS), SHAPE_CACTUS, true);
            case "voidbramble" ->
                    new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.SWEET_BERRY_BUSH), SHAPE_VOID_BRAMBLE, false);
            case "cauldron" -> new WitchesCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON));
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

    private static BlockBehaviour.Properties spiritProperties() {
        return BlockBehaviour.Properties.copy(Blocks.WATER).strength(100.0F).noCollission().noLootTable().lightLevel(state -> 3);
    }

    private static final class LegacyNonSolidShapeBlock extends Block {
        private final VoxelShape shape;
        private final boolean hasCollision;

        private LegacyNonSolidShapeBlock(BlockBehaviour.Properties properties, VoxelShape shape, boolean hasCollision) {
            super(hasCollision ? properties.noOcclusion().dynamicShape() : properties.noOcclusion().noCollission());
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
        private LegacyWitchLogBlock() {
            super(BlockBehaviour.Properties.copy(Blocks.OAK_LOG));
            registerDefaultState(defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.ROWAN));
        }

        @Override
        public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                                  @Nullable BlockEntity entity, ItemStack tool) {
            if (level instanceof ServerLevel server) Ent.tryAwaken(server, pos, this);
            super.playerDestroy(level, player, pos, state, entity, tool);
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return state.getValue(WitchWoodType.PROPERTY).stack(this);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(WitchWoodType.PROPERTY);
        }
    }

    private static final class LegacyWitchWoodVariantBlock extends Block {
        private LegacyWitchWoodVariantBlock(BlockBehaviour.Properties properties) {
            super(properties);
            registerDefaultState(defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.ROWAN));
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return state.getValue(WitchWoodType.PROPERTY).stack(this);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(WitchWoodType.PROPERTY);
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
