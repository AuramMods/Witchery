package art.arcane.witchery.registry;

import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BarrierBlockEntity;
import art.arcane.witchery.block.entity.BloodRoseBlockEntity;
import art.arcane.witchery.block.entity.BloodCrucibleBlockEntity;
import art.arcane.witchery.block.entity.ChaliceBlockEntity;
import art.arcane.witchery.block.entity.CoffinBlockEntity;
import art.arcane.witchery.block.entity.CrystalBallBlockEntity;
import art.arcane.witchery.block.entity.DemonHeartBlockEntity;
import art.arcane.witchery.block.entity.DistilleryBlockEntity;
import art.arcane.witchery.block.entity.DreamCatcherBlockEntity;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.block.entity.HuntingTrapBlockEntity;
import art.arcane.witchery.block.entity.KettleBlockEntity;
import art.arcane.witchery.block.entity.LeechChestBlockEntity;
import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.block.entity.PlacedItemBlockEntity;
import art.arcane.witchery.block.entity.PoppetShelfBlockEntity;
import art.arcane.witchery.block.entity.RefillingChestBlockEntity;
import art.arcane.witchery.block.entity.RiteHeartBlockEntity;
import art.arcane.witchery.block.entity.SilverVatBlockEntity;
import art.arcane.witchery.block.entity.SpinningWheelBlockEntity;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.block.entity.WitchesOvenBlockEntity;
import art.arcane.witchery.block.entity.WolfHeadBlockEntity;
import art.arcane.witchery.block.entity.WolfAltarBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Witchery.MODID);
    public static final Map<String, RegistryObject<? extends BlockEntityType<?>>> LEGACY_BLOCK_ENTITIES = new LinkedHashMap<>();
    public static final RegistryObject<BlockEntityType<WitchesOvenBlockEntity>> WITCHES_OVEN =
            BLOCK_ENTITIES.register("witchesovenidle", () -> ovenType(WitcheryBlockEntities::createOven));
    public static final RegistryObject<BlockEntityType<WitchesOvenBlockEntity>> LEGACY_BURNING_OVEN =
            BLOCK_ENTITIES.register("witchesovenburning", () -> ovenType(WitcheryBlockEntities::createLegacyBurningOven));
    public static final RegistryObject<BlockEntityType<WitchesCauldronBlockEntity>> CAULDRON =
            BLOCK_ENTITIES.register("cauldron", () -> BlockEntityType.Builder.of(WitchesCauldronBlockEntity::new,
                    WitcheryBlocks.getRequired("cauldron").get()).build(null));
    public static final RegistryObject<BlockEntityType<BrewFluidBlockEntity>> BREW_GAS =
            BLOCK_ENTITIES.register("brewgas", () -> brewFluidType(WitcheryBlockEntities::createBrewGas));
    public static final RegistryObject<BlockEntityType<BrewFluidBlockEntity>> BREW_LIQUID =
            BLOCK_ENTITIES.register("brewliquid", () -> brewFluidType(WitcheryBlockEntities::createBrewLiquid));
    public static final RegistryObject<BlockEntityType<AltarBlockEntity>> ALTAR =
            BLOCK_ENTITIES.register("altar", () -> BlockEntityType.Builder.of(AltarBlockEntity::new,
                    WitcheryBlocks.getRequired("altar").get()).build(null));
    public static final RegistryObject<BlockEntityType<ChaliceBlockEntity>> CHALICE =
            BLOCK_ENTITIES.register("chalice", () -> BlockEntityType.Builder.of(ChaliceBlockEntity::new,
                    WitcheryBlocks.getRequired("chalice").get()).build(null));
    public static final RegistryObject<BlockEntityType<CrystalBallBlockEntity>> CRYSTAL_BALL =
            BLOCK_ENTITIES.register("crystalball", () -> BlockEntityType.Builder.of(CrystalBallBlockEntity::new,
                    WitcheryBlocks.getRequired("crystalball").get()).build(null));
    public static final RegistryObject<BlockEntityType<DreamCatcherBlockEntity>> DREAM_CATCHER =
            BLOCK_ENTITIES.register("dreamcatcher", () -> BlockEntityType.Builder.of(DreamCatcherBlockEntity::new,
                    WitcheryBlocks.getRequired("dreamcatcher").get()).build(null));
    public static final RegistryObject<BlockEntityType<GrassperBlockEntity>> GRASSPER =
            BLOCK_ENTITIES.register("grassper", () -> BlockEntityType.Builder.of(GrassperBlockEntity::new,
                    WitcheryBlocks.getRequired("grassper").get()).build(null));
    public static final RegistryObject<BlockEntityType<DemonHeartBlockEntity>> DEMON_HEART =
            BLOCK_ENTITIES.register("demonheart", () -> BlockEntityType.Builder.of(DemonHeartBlockEntity::new,
                    WitcheryBlocks.getRequired("demonheart").get()).build(null));
    public static final RegistryObject<BlockEntityType<DistilleryBlockEntity>> DISTILLERY =
            BLOCK_ENTITIES.register("distilleryidle", () -> distilleryType(WitcheryBlockEntities::createDistillery));
    public static final RegistryObject<BlockEntityType<DistilleryBlockEntity>> LEGACY_BURNING_DISTILLERY =
            BLOCK_ENTITIES.register("distilleryburning", () -> distilleryType(WitcheryBlockEntities::createLegacyBurningDistillery));
    public static final RegistryObject<BlockEntityType<KettleBlockEntity>> KETTLE =
            BLOCK_ENTITIES.register("kettle", () -> BlockEntityType.Builder.of(KettleBlockEntity::new,
                    WitcheryBlocks.getRequired("kettle").get()).build(null));
    public static final RegistryObject<BlockEntityType<SpinningWheelBlockEntity>> SPINNING_WHEEL =
            BLOCK_ENTITIES.register("spinningwheel", () -> BlockEntityType.Builder.of(SpinningWheelBlockEntity::new,
                    WitcheryBlocks.getRequired("spinningwheel").get()).build(null));
    public static final RegistryObject<BlockEntityType<RiteHeartBlockEntity>> CIRCLE =
            BLOCK_ENTITIES.register("circle", () -> BlockEntityType.Builder.of(RiteHeartBlockEntity::new,
                    WitcheryBlocks.getRequired("circle").get()).build(null));
    public static final RegistryObject<BlockEntityType<PlacedItemBlockEntity>> PLACED_ITEM =
            BLOCK_ENTITIES.register("placeditem", () -> BlockEntityType.Builder.of(PlacedItemBlockEntity::new,
                    WitcheryBlocks.getRequired("placeditem").get()).build(null));
    public static final RegistryObject<BlockEntityType<MirrorBlockEntity>> MIRROR =
            BLOCK_ENTITIES.register("mirrorblock", () -> BlockEntityType.Builder.of(WitcheryBlockEntities::createMirror,
                    WitcheryBlocks.getRequired("mirrorblock").get()).build(null));
    public static final RegistryObject<BlockEntityType<MirrorBlockEntity>> ROOM_MIRROR =
            BLOCK_ENTITIES.register("mirrorblock2", () -> BlockEntityType.Builder.of(WitcheryBlockEntities::createRoomMirror,
                    WitcheryBlocks.getRequired("mirrorblock2").get()).build(null));
    public static final RegistryObject<BlockEntityType<RefillingChestBlockEntity>> REFILLING_CHEST =
            BLOCK_ENTITIES.register("refillingchest", () -> BlockEntityType.Builder.of(RefillingChestBlockEntity::new,
                    WitcheryBlocks.getRequired("refillingchest").get()).build(null));
    public static final RegistryObject<BlockEntityType<LeechChestBlockEntity>> LEECH_CHEST =
            BLOCK_ENTITIES.register("leechchest", () -> BlockEntityType.Builder.of(LeechChestBlockEntity::new,
                    WitcheryBlocks.getRequired("leechchest").get()).build(null));
    public static final RegistryObject<BlockEntityType<BloodRoseBlockEntity>> BLOOD_ROSE =
            BLOCK_ENTITIES.register("bloodrose", () -> BlockEntityType.Builder.of(BloodRoseBlockEntity::new,
                    WitcheryBlocks.getRequired("bloodrose").get()).build(null));
    public static final RegistryObject<BlockEntityType<BloodCrucibleBlockEntity>> BLOOD_CRUCIBLE =
            BLOCK_ENTITIES.register("bloodcrucible", () -> BlockEntityType.Builder.of(BloodCrucibleBlockEntity::new,
                    WitcheryBlocks.getRequired("bloodcrucible").get()).build(null));
    public static final RegistryObject<BlockEntityType<SilverVatBlockEntity>> SILVER_VAT =
            BLOCK_ENTITIES.register("silvervat", () -> BlockEntityType.Builder.of(SilverVatBlockEntity::new,
                    WitcheryBlocks.getRequired("silvervat").get()).build(null));
    public static final RegistryObject<BlockEntityType<CoffinBlockEntity>> COFFIN =
            BLOCK_ENTITIES.register("coffinblock", () -> BlockEntityType.Builder.of(CoffinBlockEntity::new,
                    WitcheryBlocks.getRequired("coffinblock").get()).build(null));
    public static final RegistryObject<BlockEntityType<BarrierBlockEntity>> BARRIER =
            BLOCK_ENTITIES.register("barrier", () -> BlockEntityType.Builder.of(BarrierBlockEntity::new,
                    WitcheryBlocks.getRequired("barrier").get()).build(null));
    public static final RegistryObject<BlockEntityType<PoppetShelfBlockEntity>> POPPET_SHELF =
            BLOCK_ENTITIES.register("poppetshelf", () -> BlockEntityType.Builder.of(PoppetShelfBlockEntity::new,
                    WitcheryBlocks.getRequired("poppetshelf").get()).build(null));
    public static final RegistryObject<BlockEntityType<WolfHeadBlockEntity>> WOLF_HEAD =
            BLOCK_ENTITIES.register("wolfhead", () -> BlockEntityType.Builder.of(WolfHeadBlockEntity::new,
                    WitcheryBlocks.getRequired("wolfhead").get()).build(null));
    public static final RegistryObject<BlockEntityType<WolfAltarBlockEntity>> WOLF_ALTAR =
            BLOCK_ENTITIES.register("wolfaltar", () -> BlockEntityType.Builder.of(WolfAltarBlockEntity::new,
                    WitcheryBlocks.getRequired("wolfaltar").get()).build(null));
    public static final RegistryObject<BlockEntityType<HuntingTrapBlockEntity>> HUNTING_TRAP =
            BLOCK_ENTITIES.register("beartrap", () -> BlockEntityType.Builder.of(HuntingTrapBlockEntity::new,
                    WitcheryBlocks.getRequired("beartrap").get(), WitcheryBlocks.getRequired("wolftrap").get()).build(null));

    static {
        LEGACY_BLOCK_ENTITIES.put("beartrap", HUNTING_TRAP);
        LEGACY_BLOCK_ENTITIES.put("altar", ALTAR);
        LEGACY_BLOCK_ENTITIES.put("barrier", BARRIER);
        LEGACY_BLOCK_ENTITIES.put("poppetshelf", POPPET_SHELF);
        LEGACY_BLOCK_ENTITIES.put("wolfhead", WOLF_HEAD);
        LEGACY_BLOCK_ENTITIES.put("wolfaltar", WOLF_ALTAR);
        LEGACY_BLOCK_ENTITIES.put("chalice", CHALICE);
        LEGACY_BLOCK_ENTITIES.put("crystalball", CRYSTAL_BALL);
        LEGACY_BLOCK_ENTITIES.put("dreamcatcher", DREAM_CATCHER);
        LEGACY_BLOCK_ENTITIES.put("grassper", GRASSPER);
        LEGACY_BLOCK_ENTITIES.put("demonheart", DEMON_HEART);
        LEGACY_BLOCK_ENTITIES.put("circle", CIRCLE);
        LEGACY_BLOCK_ENTITIES.put("placeditem", PLACED_ITEM);
        LEGACY_BLOCK_ENTITIES.put("distilleryidle", DISTILLERY);
        LEGACY_BLOCK_ENTITIES.put("distilleryburning", LEGACY_BURNING_DISTILLERY);
        LEGACY_BLOCK_ENTITIES.put("kettle", KETTLE);
        LEGACY_BLOCK_ENTITIES.put("mirrorblock", MIRROR);
        LEGACY_BLOCK_ENTITIES.put("mirrorblock2", ROOM_MIRROR);
        LEGACY_BLOCK_ENTITIES.put("refillingchest", REFILLING_CHEST);
        LEGACY_BLOCK_ENTITIES.put("leechchest", LEECH_CHEST);
        LEGACY_BLOCK_ENTITIES.put("bloodrose", BLOOD_ROSE);
        LEGACY_BLOCK_ENTITIES.put("bloodcrucible", BLOOD_CRUCIBLE);
        LEGACY_BLOCK_ENTITIES.put("silvervat", SILVER_VAT);
        LEGACY_BLOCK_ENTITIES.put("coffinblock", COFFIN);
        LEGACY_BLOCK_ENTITIES.put("spinningwheel", SPINNING_WHEEL);
        LEGACY_BLOCK_ENTITIES.put("cauldron", CAULDRON);
        LEGACY_BLOCK_ENTITIES.put("brewgas", BREW_GAS);
        LEGACY_BLOCK_ENTITIES.put("brewliquid", BREW_LIQUID);
        LEGACY_BLOCK_ENTITIES.put("witchesovenidle", WITCHES_OVEN);
        LEGACY_BLOCK_ENTITIES.put("witchesovenburning", LEGACY_BURNING_OVEN);
        for (String legacyName : LegacyRegistryData.BLOCK_ENTITIES) {
            registerLegacyBlockEntity(legacyName);
        }
    }

    private WitcheryBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

    public static Map<String, RegistryObject<? extends BlockEntityType<?>>> all() {
        return Collections.unmodifiableMap(LEGACY_BLOCK_ENTITIES);
    }

    private static BlockEntityType<WitchesOvenBlockEntity> ovenType(BlockEntityType.BlockEntitySupplier<WitchesOvenBlockEntity> factory) {
        return BlockEntityType.Builder.of(factory,
                WitcheryBlocks.getRequired("witchesovenidle").get(),
                WitcheryBlocks.getRequired("witchesovenburning").get()).build(null);
    }

    private static BlockEntityType<BrewFluidBlockEntity> brewFluidType(BlockEntityType.BlockEntitySupplier<BrewFluidBlockEntity> factory) {
        return BlockEntityType.Builder.of(factory,
                WitcheryBlocks.getRequired("brewgas").get(), WitcheryBlocks.getRequired("brewliquid").get()).build(null);
    }

    private static BrewFluidBlockEntity createBrewGas(BlockPos pos, BlockState state) {
        return new BrewFluidBlockEntity(BREW_GAS.get(), pos, state);
    }

    private static BrewFluidBlockEntity createBrewLiquid(BlockPos pos, BlockState state) {
        return new BrewFluidBlockEntity(BREW_LIQUID.get(), pos, state);
    }

    private static MirrorBlockEntity createMirror(BlockPos pos, BlockState state) {
        return new MirrorBlockEntity(MIRROR.get(), pos, state);
    }

    private static MirrorBlockEntity createRoomMirror(BlockPos pos, BlockState state) {
        return new MirrorBlockEntity(ROOM_MIRROR.get(), pos, state);
    }

    private static WitchesOvenBlockEntity createOven(BlockPos pos, BlockState state) {
        return new WitchesOvenBlockEntity(WITCHES_OVEN.get(), pos, state);
    }

    private static WitchesOvenBlockEntity createLegacyBurningOven(BlockPos pos, BlockState state) {
        return new WitchesOvenBlockEntity(LEGACY_BURNING_OVEN.get(), pos, state);
    }

    private static BlockEntityType<DistilleryBlockEntity> distilleryType(BlockEntityType.BlockEntitySupplier<DistilleryBlockEntity> factory) {
        return BlockEntityType.Builder.of(factory,
                WitcheryBlocks.getRequired("distilleryidle").get(),
                WitcheryBlocks.getRequired("distilleryburning").get()).build(null);
    }

    private static DistilleryBlockEntity createDistillery(BlockPos pos, BlockState state) {
        return new DistilleryBlockEntity(DISTILLERY.get(), pos, state);
    }

    private static DistilleryBlockEntity createLegacyBurningDistillery(BlockPos pos, BlockState state) {
        return new DistilleryBlockEntity(LEGACY_BURNING_DISTILLERY.get(), pos, state);
    }

    @SuppressWarnings("unchecked")
    private static void registerLegacyBlockEntity(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_BLOCK_ENTITIES.containsKey(path)) {
            return;
        }

        RegistryObject<Block> block = WitcheryBlocks.getRequired(path);

        final RegistryObject<BlockEntityType<PlaceholderBlockEntity>>[] holder = (RegistryObject<BlockEntityType<PlaceholderBlockEntity>>[]) new RegistryObject<?>[1];
        holder[0] = BLOCK_ENTITIES.register(path,
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new PlaceholderBlockEntity(holder[0], pos, state),
                        block.get())
                        .build(null));

        LEGACY_BLOCK_ENTITIES.put(path, holder[0]);
    }
}
