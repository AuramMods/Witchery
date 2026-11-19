package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.block.entity.WitchesOvenBlockEntity;
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

    static {
        LEGACY_BLOCK_ENTITIES.put("cauldron", CAULDRON);
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

    private static WitchesOvenBlockEntity createOven(BlockPos pos, BlockState state) {
        return new WitchesOvenBlockEntity(WITCHES_OVEN.get(), pos, state);
    }

    private static WitchesOvenBlockEntity createLegacyBurningOven(BlockPos pos, BlockState state) {
        return new WitchesOvenBlockEntity(LEGACY_BURNING_OVEN.get(), pos, state);
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
