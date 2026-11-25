package art.arcane.witchery.registry;

import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.PitDirtBlock;
import art.arcane.witchery.block.PerpetualIceBlock;
import art.arcane.witchery.block.PerpetualIceDoorBlock;
import art.arcane.witchery.block.PerpetualIceFenceBlock;
import art.arcane.witchery.block.StockadeBlock;
import art.arcane.witchery.block.FrozenPressurePlateBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.block.GlowGlobeBlock;
import art.arcane.witchery.block.WitchDoorBlock;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BarrierBlock;
import art.arcane.witchery.block.PoppetShelfBlock;
import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.block.SpanishMossBlock;
import art.arcane.witchery.block.SpreadingHerbBlock;
import art.arcane.witchery.block.StatueGoddessBlock;
import art.arcane.witchery.block.VoidBrambleBlock;
import art.arcane.witchery.block.AlluringSkullBlock;
import art.arcane.witchery.block.AreaWardBlock;
import art.arcane.witchery.block.CandelabraBlock;
import art.arcane.witchery.block.ChaliceBlock;
import art.arcane.witchery.block.CoffinBlock;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.CursedControls;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.BrazierBlock;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.BrewPlantBlocks;
import art.arcane.witchery.block.BloodRoseBlock;
import art.arcane.witchery.block.BloodCrucibleBlock;
import art.arcane.witchery.block.GarlicGarlandBlock;
import art.arcane.witchery.block.SilverVatBlock;
import art.arcane.witchery.block.ShadedGlassBlock;
import art.arcane.witchery.block.CrystalBallBlock;
import art.arcane.witchery.block.DaylightCollectorBlock;
import art.arcane.witchery.block.DemonHeartBlock;
import art.arcane.witchery.block.DiseaseBlock;
import art.arcane.witchery.block.DistilleryBlock;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.FetishBlock;
import art.arcane.witchery.block.FumeFunnelBlock;
import art.arcane.witchery.block.GrassperBlock;
import art.arcane.witchery.block.HuntingTrapBlock;
import art.arcane.witchery.block.KettleBlock;
import art.arcane.witchery.block.LeechChestBlock;
import art.arcane.witchery.block.LeapingLilyBlock;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.RefillingChestBlock;
import art.arcane.witchery.block.SlurpBlock;
import art.arcane.witchery.block.SomnianCottonBlock;
import art.arcane.witchery.block.SpinningWheelBlock;
import art.arcane.witchery.block.SpiritFluidBlock;
import art.arcane.witchery.block.SpiritPortalBlock;
import art.arcane.witchery.block.SunlightBlock;
import art.arcane.witchery.block.TormentPortalBlock;
import art.arcane.witchery.block.PlacedItemBlock;
import art.arcane.witchery.block.StatueOfWorshipBlock;
import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.block.WitchCropBlock;
import art.arcane.witchery.block.WitchLeavesBlock;
import art.arcane.witchery.block.WitchLilyPadBlock;
import art.arcane.witchery.block.WitchSaplingBlock;
import art.arcane.witchery.block.WitchWoodStairBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.WitchWoodSlabBlock;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.WitchesOvenBlock;
import art.arcane.witchery.block.WolfHeadBlock;
import art.arcane.witchery.block.WolfAltarBlock;
import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import art.arcane.witchery.entity.Ent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
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
    private static final VoxelShape SHAPE_INFINITY_EGG = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Witchery.MODID);
    public static final RegistryObject<SlabBlock> APOTHECARY_SHELF = BLOCKS.register("apothecary_shelf",
            () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.BIRCH_SLAB)));
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
            case "witchwood" -> new LegacyWitchWoodVariantBlock(
                    BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).explosionResistance(2));
            case "witchleaves" -> new WitchLeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES));
            case "witchsapling" -> new WitchSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING));
            case "wickerbundle" -> new WickerBundleBlock();
            case "perpetualice" -> new PerpetualIceBlock();
            case "shadedglass" -> new ShadedGlassBlock(false);
            case "shadedglass_active" -> new ShadedGlassBlock(true);
            case "bloodrose" -> new BloodRoseBlock();
            case "bloodedwool" -> new Block(BlockBehaviour.Properties.copy(Blocks.RED_WOOL));
            case "plantmine" -> new PlantMineBlock();
            case "glintweed" -> new SpreadingHerbBlock.GlintWeed();
            case "spanishmoss" -> new SpanishMossBlock();
            case "vine" -> new BrewPlantBlocks.Vine();
            case "web" -> new BrewPlantBlocks.Web();
            case "bramble" -> new BrambleBlock();
            case "crittersnare" -> new CritterSnareBlock();
            case "somniancotton" -> new SomnianCottonBlock(BlockBehaviour.Properties.copy(Blocks.DANDELION).randomTicks());
            case "embermoss" -> new SpreadingHerbBlock.EmberMoss();
            case "lilypad" -> new WitchLilyPadBlock();
            case "leapinglily" -> new LeapingLilyBlock();
            case "grassper" -> new GrassperBlock();
            case "icedoor" -> new PerpetualIceDoorBlock();
            case "rowanwooddoor" -> new WitchDoorBlock(false);
            case "alderwooddoor" -> new WitchDoorBlock(true);
            case "cwoodendoor" -> new CursedControls.Door();
            case "altar" -> new AltarBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0F));
            case "kettle" -> new KettleBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(2.0F).noOcclusion());
            case "spinningwheel" ->
                    new SpinningWheelBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(3.5F).noOcclusion());
            case "distilleryidle", "distilleryburning" ->
                    new DistilleryBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(3.5F).noOcclusion()
                            .lightLevel(state -> state.getValue(DistilleryBlock.LIT) ? 6 : 0));
            case "witchesovenidle", "witchesovenburning" ->
                    new WitchesOvenBlock(BlockBehaviour.Properties.copy(Blocks.FURNACE)
                            .strength(3.5F)
                            .noOcclusion()
                            .lightLevel(state -> state.getValue(WitchesOvenBlock.LIT) ? 13 : 0));
            case "fumefunnel", "filteredfumefunnel" ->
                    new FumeFunnelBlock(BlockBehaviour.Properties.copy(Blocks.STONE).strength(3.5F).noOcclusion());
            case "coffinblock" -> new CoffinBlock();
            case "statueofworship" -> new StatueOfWorshipBlock();
            case "refillingchest" -> new RefillingChestBlock();
            case "leechchest" -> new LeechChestBlock();
            case "stairswoodrowan" -> new WitchWoodStairBlock(WitchWoodType.ROWAN);
            case "stairswoodalder" -> new WitchWoodStairBlock(WitchWoodType.ALDER);
            case "stairswoodhawthorn" -> new WitchWoodStairBlock(WitchWoodType.HAWTHORN);
            case "icestairs" -> new StairBlock(() -> getRequired("perpetualice").get().defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.GLASS).mapColor(MapColor.ICE).friction(0.98F).strength(2, 3));
            case "snowstairs" -> new StairBlock(Blocks.SNOW_BLOCK.defaultBlockState(),
                    BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK));
            case "witchwoodslab", "witchwooddoubleslab" -> new WitchWoodSlabBlock(path.equals("witchwooddoubleslab"));
            case "iceslab" -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.GLASS).mapColor(MapColor.ICE)
                    .sound(SoundType.STONE).strength(2, 3));
            case "snowslab" -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK));
            case "icedoubleslab" -> new Block(BlockBehaviour.Properties.copy(Blocks.ICE).strength(2, 3));
            case "snowdoubleslab" -> new Block(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK));
            case "stockade" -> new StockadeBlock.Wooden();
            case "icestockade" -> new StockadeBlock(true);
            case "icefence" -> new PerpetualIceFenceBlock();
            case "icefencegate" -> new FenceGateBlock(BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE).strength(2, 3), WoodType.OAK);
            case "icepressureplate" -> new FrozenPressurePlateBlock(true);
            case "snowpressureplate" -> new FrozenPressurePlateBlock(false);
            case "cwoodpressureplate" -> new CursedControls.Plate(CursedControls.Kind.WOODEN_PLATE);
            case "csnowpressureplate" -> new CursedControls.Plate(CursedControls.Kind.SNOW_PLATE);
            case "cstonepressureplate" -> new CursedControls.Plate(CursedControls.Kind.STONE_PLATE);
            case "cbuttonwood" -> new CursedControls.Button(true);
            case "cbuttonstone" -> new CursedControls.Button(false);
            case "clever" -> new CursedControls.Lever();
            case "belladonna", "mandrake", "artichoke", "snowbell", "wormwood", "mindrake", "garlicplant", "wolfsbane" ->
                    new WitchCropBlock(WitchCrop.forBlock(path), BlockBehaviour.Properties.copy(Blocks.WHEAT));
            case "wolftrap" -> new HuntingTrapBlock(true);
            case "beartrap" -> new HuntingTrapBlock(false);
            case "demonheart" ->
                    new DemonHeartBlock(BlockBehaviour.Properties.copy(Blocks.DIRT).strength(1).noOcclusion()
                            .sound(SoundType.NETHERRACK).lightLevel(state -> 3));
            case "wolfhead" -> new WolfHeadBlock();
            case "alluringskull" -> new AlluringSkullBlock();
            case "chalice" ->
                    new ChaliceBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).noOcclusion().strength(3.0F));
            case "candelabra" ->
                    new CandelabraBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BARS).noOcclusion().strength(2.0F).lightLevel(state -> 15));
            case "brazier" -> new BrazierBlock();
            case "crystalball" -> new CrystalBallBlock();
            case "decursedirected" -> new AreaWardBlock(AreaWardBlockEntity.Kind.DIRECTED);
            case "decurseteleport" -> new AreaWardBlock(AreaWardBlockEntity.Kind.TELEPORT);
            case "scarecrow", "trent", "witchsladder" -> new FetishBlock(path);
            case "placeditem" -> new PlacedItemBlock();
            case "glowglobe" -> new GlowGlobeBlock();
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
                    .strength(path.equals("mirrorblock") ? 1 : -1, 5_999.4F).noLootTable(), path.equals("mirrorblock2"));
            case "circle" -> new RiteHeartBlock();
            case "circleglyphritual" -> new RitualGlyphBlock("chalkritual");
            case "circleglyphotherwhere" -> new RitualGlyphBlock("chalkotherwhere");
            case "circleglyphinfernal" -> new RitualGlyphBlock("chalkinfernal");
            case "spiritportal" -> new SpiritPortalBlock(BlockBehaviour.Properties.copy(Blocks.NETHER_PORTAL).noLootTable());
            case "mirrorwall" -> new GlassBlock(BlockBehaviour.Properties.copy(Blocks.GLASS)
                    .strength(-1, 5_999.4F).noLootTable());
            case "tormentportal" -> new TormentPortalBlock();
            case "tormentstone" -> new Block(BlockBehaviour.Properties.copy(Blocks.MYCELIUM)
                    .strength(-1, 5_999.4F).noLootTable().noOcclusion());
            case "spiritflowing" -> new SpiritFluidBlock(WitcheryFluids.getRequiredSource("fluidspirit"), spiritProperties(), true);
            case "hollowtears" -> new SpiritFluidBlock(WitcheryFluids.getRequiredSource("hollowtears"), spiritProperties(), false);
            case "disease" -> new DiseaseBlock();
            case "brew" -> new LiquidBlock(WitcheryFluids.getRequiredSource("brew"),
                    BlockBehaviour.Properties.copy(Blocks.WATER).instabreak().noCollission().noLootTable());
            case "brewgas" -> new BrewDispersalBlock(true);
            case "brewliquid" -> new BrewDispersalBlock(false);
            case "force" -> new LegacyNonSolidShapeBlock(BlockBehaviour.Properties.copy(Blocks.BARRIER)
                    .explosionResistance(5_999.4F).noLootTable(), SHAPE_FULL_BLOCK, true);
            case "barrier" -> new BarrierBlock();
            case "light" -> new SunlightBlock();
            case "slurp" -> new SlurpBlock();
            case "cactus" -> new BrewPlantBlocks.Cactus();
            case "voidbramble" -> new VoidBrambleBlock();
            case "cauldron" -> new WitchesCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON));
            case "wolfaltar" -> new WolfAltarBlock();
            case "statuegoddess" -> new StatueGoddessBlock();
            case "pitdirt" -> new PitDirtBlock();
            case "pitgrass" -> new net.minecraft.world.level.block.SnowyDirtBlock(
                    BlockBehaviour.Properties.copy(Blocks.GRASS_BLOCK).noOcclusion().noCollission());
            case "wallgen" -> new Block(BlockBehaviour.Properties.copy(Blocks.BEDROCK)
                    .strength(-1.0F, 6_000.0F)
                    .noOcclusion()
                    .noLootTable()
                    .noCollission());
            default -> new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
        };
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

}
