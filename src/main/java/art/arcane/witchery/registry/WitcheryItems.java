package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WitcheryItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Witchery.MODID);
    public static final Map<String, RegistryObject<Item>> LEGACY_ITEMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> LEGACY_BLOCK_ITEMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> LEGACY_SPAWN_EGGS = new LinkedHashMap<>();

    static {
        for (String legacyName : LegacyRegistryData.ITEMS) {
            registerLegacyItem(legacyName);
        }

        for (String legacyName : WitcheryEntities.all().keySet()) {
            registerLegacySpawnEgg(legacyName);
        }

        for (String legacyName : LegacyRegistryData.BLOCKS) {
            registerLegacyBlockItem(legacyName);
        }
    }

    private WitcheryItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static RegistryObject<Item> getRequired(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        RegistryObject<Item> item = LEGACY_ITEMS.get(path);
        if (item == null) {
            throw new IllegalStateException("Missing legacy item placeholder for path: " + path);
        }
        return item;
    }

    public static Collection<RegistryObject<Item>> all() {
        return Collections.unmodifiableCollection(LEGACY_ITEMS.values());
    }

    public static Collection<RegistryObject<Item>> allForCreativeTab() {
        List<RegistryObject<Item>> combined = new ArrayList<>(LEGACY_ITEMS.size() + LEGACY_BLOCK_ITEMS.size() + LEGACY_SPAWN_EGGS.size());
        combined.addAll(LEGACY_ITEMS.values());
        combined.addAll(LEGACY_SPAWN_EGGS.values());
        combined.addAll(LEGACY_BLOCK_ITEMS.values());
        return Collections.unmodifiableList(combined);
    }

    private static void registerLegacyItem(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);
        if (LEGACY_ITEMS.containsKey(path)) {
            return;
        }

        RegistryObject<Item> item = ITEMS.register(path, () -> createLegacyItem(path));
        LEGACY_ITEMS.put(path, item);
    }

    private static Item createLegacyItem(String path) {
        Item.Properties bucketProperties = new Item.Properties()
                .craftRemainder(Items.BUCKET)
                .stacksTo(1);
        return switch (path) {
            case "bucketspirit" -> new BucketItem(WitcheryFluids.getRequiredSource("fluidspirit"), bucketProperties);
            case "buckethollowtears" -> new BucketItem(WitcheryFluids.getRequiredSource("hollowtears"), bucketProperties);
            case "bucketbrew" -> new BucketItem(WitcheryFluids.getRequiredSource("brew"), bucketProperties);
            default -> new Item(new Item.Properties());
        };
    }

    private static void registerLegacySpawnEgg(String legacyEntityName) {
        String path = RegistryNameUtil.normalizePath(legacyEntityName);
        String eggPath = path + "_spawn_egg";

        if (LEGACY_ITEMS.containsKey(eggPath)
                || LEGACY_BLOCK_ITEMS.containsKey(eggPath)
                || LEGACY_SPAWN_EGGS.containsKey(eggPath)) {
            return;
        }

        RegistryObject<EntityType<Zombie>> entityType = WitcheryEntities.all().get(path);
        if (entityType == null) {
            throw new IllegalStateException("Missing legacy entity placeholder for path: " + path);
        }

        RegistryObject<Item> egg = ITEMS.register(eggPath,
                () -> new ForgeSpawnEggItem(entityType, 0x6E5A4E, 0xA4A29D, new Item.Properties()));
        LEGACY_SPAWN_EGGS.put(eggPath, egg);
    }

    private static void registerLegacyBlockItem(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);

        if (LEGACY_ITEMS.containsKey(path) || LEGACY_BLOCK_ITEMS.containsKey(path)) {
            return;
        }

        RegistryObject<Block> block = WitcheryBlocks.getRequired(path);
        RegistryObject<Item> item = ITEMS.register(path, () -> {
            Block b = block.get();
            return b instanceof DoorBlock
                    ? new DoubleHighBlockItem(b, new Item.Properties())
                    : new BlockItem(b, new Item.Properties());
        });
        LEGACY_BLOCK_ITEMS.put(path, item);
    }
}
