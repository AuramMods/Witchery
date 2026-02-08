package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class WitcheryBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Witchery.MODID);
    public static final Map<String, RegistryObject<BlockEntityType<PlaceholderBlockEntity>>> LEGACY_BLOCK_ENTITIES = new LinkedHashMap<>();

    static {
        for (String legacyName : LegacyRegistryData.BLOCK_ENTITIES) {
            registerLegacyBlockEntity(legacyName);
        }
    }

    private WitcheryBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }

    public static Map<String, RegistryObject<BlockEntityType<PlaceholderBlockEntity>>> all() {
        return Collections.unmodifiableMap(LEGACY_BLOCK_ENTITIES);
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
