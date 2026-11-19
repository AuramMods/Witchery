package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.item.AnointingPasteItem;
import art.arcane.witchery.item.MutandisItem;
import art.arcane.witchery.item.RitualChalkItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.WitchWoodBlockItem;
import art.arcane.witchery.item.WitchSeedsItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
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

        for (WitcheryIngredient ingredient : WitcheryIngredient.values()) {
            registerLegacyItem(ingredient.path());
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
            throw new IllegalStateException("Missing Witchery item for path: " + path);
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
        WitchCrop crop = WitchCrop.forSeed(path);
        if (crop != null) {
            return crop == WitchCrop.ARTICHOKE
                    ? new WitchSeedsItem.WaterSeeds(crop, new Item.Properties())
                    : new WitchSeedsItem(crop, new Item.Properties());
        }
        Item.Properties bucketProperties = new Item.Properties()
                .craftRemainder(Items.BUCKET)
                .stacksTo(1);
        return switch (path) {
            case "bucketspirit" -> new BucketItem(WitcheryFluids.getRequiredSource("fluidspirit"), bucketProperties);
            case "buckethollowtears" -> new BucketItem(WitcheryFluids.getRequiredSource("hollowtears"), bucketProperties);
            case "bucketbrew" -> new BucketItem(WitcheryFluids.getRequiredSource("brew"), bucketProperties);
            case "anointing_paste" -> new AnointingPasteItem(new Item.Properties());
            case "mutandis" -> new MutandisItem(new Item.Properties());
            case "chalkritual" -> new RitualChalkItem("circleglyphritual");
            case "chalkotherwhere" -> new RitualChalkItem("circleglyphotherwhere");
            case "chalkinfernal" -> new RitualChalkItem("circleglyphinfernal");
            case "chalkheart" -> new RitualChalkItem("circle");
            case "rowan_berries" -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(1).saturationMod(6.0F).build()));
            case "globe_artichoke" -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(20).saturationMod(0F).build()));
            case "earmuffs" -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties());
            case "witchwoodslab", "witchwooddoubleslab", "iceslab", "icedoubleslab", "snowslab", "snowdoubleslab" ->
                    createBlockItem(path);
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

        RegistryObject<? extends EntityType<? extends Mob>> entityType = WitcheryEntities.all().get(path);
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

        RegistryObject<Item> item = ITEMS.register(path, () -> createBlockItem(path));
        LEGACY_BLOCK_ITEMS.put(path, item);
    }

    private static BlockItem createBlockItem(String path) {
        Block block = WitcheryBlocks.getRequired(path).get();
        switch (path) {
            case "witchsapling", "witchleaves", "witchlog", "witchwood" -> {
                return new WitchWoodBlockItem(block, new Item.Properties());
            }
        }
        return block instanceof DoorBlock
                ? new DoubleHighBlockItem(block, new Item.Properties())
                : new BlockItem(block, new Item.Properties());
    }
}
