package art.arcane.witchery.registry;

import art.arcane.witchery.item.BrewBucketItem;
import art.arcane.witchery.item.DivinerItem;
import art.arcane.witchery.item.StatueGoddessItem;
import art.arcane.witchery.item.AreaWardItem;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.LeapingLilyBlock;
import art.arcane.witchery.block.ShadedGlassBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.block.StatueOfWorshipBlock;
import art.arcane.witchery.item.KobolditePentacleItem;
import art.arcane.witchery.item.GoblinEquipmentItem;
import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.item.SpiritOfferingItem;
import art.arcane.witchery.item.SpectralStoneItem;

import art.arcane.witchery.item.BrewFuelItem;
import art.arcane.witchery.item.EndlessWaterBrewItem;
import art.arcane.witchery.item.PlayerCompassItem;
import art.arcane.witchery.item.PoppetShelfCompassItem;
import art.arcane.witchery.item.ParasyticLouseItem;
import art.arcane.witchery.item.TreefydSeedsItem;
import art.arcane.witchery.item.PolynesiaCharmItem;
import art.arcane.witchery.block.StockadeBlock;
import art.arcane.witchery.item.BarkBeltItem;
import art.arcane.witchery.item.BitingBeltItem;
import art.arcane.witchery.item.QuicklimeItem;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.brewing.BrewImpacts;
import art.arcane.witchery.brewing.BrewCreatureEffects;
import art.arcane.witchery.brewing.BrewErosion;
import art.arcane.witchery.brewing.BrewIce;
import art.arcane.witchery.brewing.BrewOfTheDepths;
import art.arcane.witchery.brewing.BrewSolidification;
import art.arcane.witchery.brewing.BrewStatusActions;
import art.arcane.witchery.brewing.BrewSubstitution;
import art.arcane.witchery.brewing.BrewTerrain;
import art.arcane.witchery.brewing.GrotesqueEffect;
import art.arcane.witchery.item.AnointingPasteItem;
import art.arcane.witchery.item.BoundPoppetItem;
import art.arcane.witchery.item.ArthanaItem;
import art.arcane.witchery.item.BolineItem;
import art.arcane.witchery.item.BrewBagItem;
import art.arcane.witchery.item.BlockEntityDisplayItem;
import art.arcane.witchery.item.AntidoteItem;
import art.arcane.witchery.item.ArtichokeItem;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.DoorKeyItem;
import art.arcane.witchery.item.BroomItem;
import art.arcane.witchery.item.CaneSwordItem;
import art.arcane.witchery.item.GuardingSwordItem;
import art.arcane.witchery.item.CircleTalismanItem;
import art.arcane.witchery.item.HandBowItem;
import art.arcane.witchery.item.HornOfTheHuntItem;
import art.arcane.witchery.item.HuntsmanSpearItem;
import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.item.HuntingTrapItem;
import art.arcane.witchery.item.HunterClothingItem.Piece;
import art.arcane.witchery.item.HunterClothingItem.Upgrade;
import art.arcane.witchery.item.DemonHeartItem;
import art.arcane.witchery.item.BinkyHeadItem;
import art.arcane.witchery.item.DeathClothingItem;
import art.arcane.witchery.item.EarmuffsItem;
import art.arcane.witchery.item.DeathHandItem;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.DeathProtectionPoppetItem;
import art.arcane.witchery.item.FetishItem;
import art.arcane.witchery.item.DrinkableBrewItem;
import art.arcane.witchery.item.IngredientFoodItem;
import art.arcane.witchery.item.DuplicationStaffItem;
import art.arcane.witchery.item.DreamWeaveItem;
import art.arcane.witchery.item.FilledChaliceItem;
import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.item.BoneNeedleItem;
import art.arcane.witchery.item.IcyNeedleItem;
import art.arcane.witchery.item.ImpSpellContractItem;
import art.arcane.witchery.item.LeechChestItem;
import art.arcane.witchery.item.MeatyStewItem;
import art.arcane.witchery.item.MirrorItem;
import art.arcane.witchery.item.MoonCharmItem;
import art.arcane.witchery.item.WolfAltarItem;
import art.arcane.witchery.item.WolfTokenItem;
import art.arcane.witchery.item.WolfsbaneFlowerItem;
import art.arcane.witchery.item.OwnershipContractItem;
import art.arcane.witchery.item.MutandisItem;
import art.arcane.witchery.item.MysticBranchItem;
import art.arcane.witchery.item.NecromanticStoneItem;
import art.arcane.witchery.item.RitualChalkItem;
import art.arcane.witchery.item.SeerStoneItem;
import art.arcane.witchery.item.SleepingAppleItem;
import art.arcane.witchery.item.GrenadeItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.MutatingSprigItem;
import art.arcane.witchery.item.TormentContractItem;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.item.VampiricPoppetItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.PoppetProtectionItem;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.item.GuideBookItem;
import art.arcane.witchery.item.BiomeNoteItem;
import art.arcane.witchery.item.WitchHandItem;
import art.arcane.witchery.item.SnowSlabItem;
import art.arcane.witchery.item.WitchWoodBlockItem;
import art.arcane.witchery.item.WitchSeedsItem;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.infusion.SpellKnowledge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
    public static final RegistryObject<Item> APOTHECARY_SHELF = ITEMS.register("apothecary_shelf",
            () -> new BlockItem(WitcheryBlocks.APOTHECARY_SHELF.get(), new Item.Properties()));
    public static final Map<String, RegistryObject<Item>> LEGACY_ITEMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> LEGACY_BLOCK_ITEMS = new LinkedHashMap<>();
    public static final Map<String, RegistryObject<Item>> LEGACY_SPAWN_EGGS = new LinkedHashMap<>();
    private static final SpawnEgg COMPATIBILITY_EGG = new SpawnEgg(-1, 0x6E5A4E, 0xA4A29D);
    private static final Map<String, SpawnEgg> ORIGINAL_SPAWN_EGGS = Map.ofEntries(
            Map.entry("demon", new SpawnEgg(6396, 9502720, 11430927)),
            Map.entry("mandrake", new SpawnEgg(6397, 128271104, 311408)),
            Map.entry("treefyd", new SpawnEgg(6398, 5781801, 11217964)),
            Map.entry("hornedhuntsman", new SpawnEgg(6399, 11523, 4007964)),
            Map.entry("ent", new SpawnEgg(6400, 5338965, 5724240)),
            Map.entry("owl", new SpawnEgg(6401, 14869218, 6049609)),
            Map.entry("toad", new SpawnEgg(6402, 5780254, 3090974)),
            Map.entry("babayaga", new SpawnEgg(6403, 7232598, 3881787)),
            Map.entry("covenwitch", new SpawnEgg(6404, 1118481, 11523)),
            Map.entry("nightmare", new SpawnEgg(6405, 983101, 0)),
            Map.entry("spectre", new SpawnEgg(6406, 1052688, 16299031)),
            Map.entry("poltergeist", new SpawnEgg(6407, 12844917, 12844917)),
            Map.entry("banshee", new SpawnEgg(6408, 13683116, 10136945)),
            Map.entry("spirit", new SpawnEgg(6409, 16753968, 15649280)),
            Map.entry("death", new SpawnEgg(6410, 0, 0)),
            Map.entry("witchhunter", new SpawnEgg(6411, 7893099, 2300953)),
            Map.entry("lordoftorment", new SpawnEgg(6412, 9502720, 3346705)),
            Map.entry("imp", new SpawnEgg(6413, 5776143, 16738816)),
            Map.entry("mindrake", new SpawnEgg(6414, 19463, 4200704)),
            Map.entry("goblin", new SpawnEgg(6415, 10752, 15616)),
            Map.entry("goblinmog", new SpawnEgg(6416, 10752, 15616)),
            Map.entry("goblingulg", new SpawnEgg(6417, 10752, 15616)),
            Map.entry("leonard", new SpawnEgg(6418, 12152634, 2818048)),
            Map.entry("lostsoul", new SpawnEgg(6419, 12152634, 2818116)),
            Map.entry("wolfman", new SpawnEgg(6420, 2960685, 6316128)),
            Map.entry("hellhound", new SpawnEgg(6421, 14181632, 5968392)),
            Map.entry("werevillager", new SpawnEgg(6422, 5651507, 12422002)),
            Map.entry("villageguard", new SpawnEgg(6423, 2236962, 5322800)),
            Map.entry("vampire", new SpawnEgg(6424, 5322800, 13369344)),
            Map.entry("lilith", new SpawnEgg(6425, 0, 2818048)),
            Map.entry("wingedmonkey", new SpawnEgg(6426, 6846848, 7574709)),
            Map.entry("reflection", new SpawnEgg(6427, 5596842, 6715391))
    );

    private record SpawnEgg(int metadata, int shell, int spots) {}

    static {
        for (String legacyName : LegacyRegistryData.ITEMS) {
            registerLegacyItem(legacyName);
        }
        registerLegacyItem("fire_protection_poppet");
        registerLegacyItem("earth_protection_poppet");
        registerLegacyItem("water_protection_poppet");
        registerLegacyItem("hunger_protection_poppet");
        registerLegacyItem("tool_protection_poppet");
        registerLegacyItem("armor_protection_poppet");
        registerLegacyItem("death_protection_poppet");
        registerLegacyItem("voodoo_poppet");
        registerLegacyItem("voodoo_protection_poppet");
        registerLegacyItem("vampiric_poppet");
        registerLegacyItem("poppet_protection");
        registerLegacyItem("hellhound_head");

        for (WitcheryIngredient ingredient : WitcheryIngredient.values()) {
            registerLegacyItem(ingredient.path());
        }

        for (String legacyName : WitcheryEntities.all().keySet()) {
            registerLegacySpawnEgg(legacyName);
        }
        // Older port saves can contain placeholder spawn eggs for these projectiles.
        for (String name : List.of("soulfire_spawn_egg", "spelleffect_spawn_egg", "grenade_spawn_egg",
                "bolt_spawn_egg", "droplet_spawn_egg", "splatter_spawn_egg")) {
            LEGACY_SPAWN_EGGS.put(name, ITEMS.register(name, () -> new Item(new Item.Properties())));
        }
        ITEMS.register("brew_spawn_egg", () -> new Item(new Item.Properties()));
        ITEMS.register("brew2_spawn_egg", () -> new Item(new Item.Properties()));
        ITEMS.register("broom_spawn_egg", () -> new BroomItem(true));
        ITEMS.register("item_spawn_egg", () -> new Item(new Item.Properties()));
        ITEMS.register("eye_spawn_egg", () -> new Item(new Item.Properties()));

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

    public static Map<Integer, String> originalSpawnEggIds() {
        Map<Integer, String> ids = new LinkedHashMap<>();
        ORIGINAL_SPAWN_EGGS.forEach((entity, definition) -> ids.put(definition.metadata(), entity));
        return Map.copyOf(ids);
    }

    public static Collection<RegistryObject<Item>> allForCreativeTab() {
        List<RegistryObject<Item>> combined = new ArrayList<>(LEGACY_ITEMS.size() + LEGACY_BLOCK_ITEMS.size() + ORIGINAL_SPAWN_EGGS.size());
        combined.addAll(LEGACY_ITEMS.values());
        for (String entity : WitcheryEntities.all().keySet()) {
            if (ORIGINAL_SPAWN_EGGS.containsKey(entity)) {
                combined.add(LEGACY_SPAWN_EGGS.get(entity + "_spawn_egg"));
            }
        }
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
            case "stewraw" -> new MeatyStewItem(1, 0.1F);
            case "stew" -> new MeatyStewItem(12, 1.0F);
            case "divinerwater" -> new DivinerItem(Blocks.WATER);
            case "divinerlava" -> new DivinerItem(Blocks.LAVA);
            case "bucketspirit" -> new BucketItem(WitcheryFluids.getRequiredSource("fluidspirit"), bucketProperties);
            case "buckethollowtears" -> new BucketItem(WitcheryFluids.getRequiredSource("hollowtears"), bucketProperties);
            case "bucketbrew" -> new BrewBucketItem();
            case "brewbottle" -> new BrewBottleItem();
            case "potion" -> new AntidoteItem();
            case "louse" -> new ParasyticLouseItem();
            case "rowanwooddoor", "alderwooddoor", "icedoor" -> createBlockItem(path);
            case "door_key", "door_keyring" -> new DoorKeyItem();
            case "anointing_paste" -> new AnointingPasteItem(new Item.Properties());
            case "taglockkit" -> new TaglockItem();
            case "coffin" -> createBlockItem("coffinblock");
            case "mooncharm" -> new MoonCharmItem();
            case "wolftoken" -> new WolfTokenItem();
            case "dupstaff" -> new DuplicationStaffItem();
            case "wolfsbane_flower" -> new WolfsbaneFlowerItem();
            case "hornofthehunt" -> new HornOfTheHuntItem();
            case "huntsmanspear" -> new HuntsmanSpearItem();
            case "hellhound_head" -> new BlockItem(WitcheryBlocks.getRequired("wolfhead").get(), new Item.Properties()) {
                @Override
                public String getDescriptionId() { return getOrCreateDescriptionId(); }

                @Override
                public void registerBlocks(Map<Block, Item> blockToItem, Item item) {}
            };
            case "canesword" -> new CaneSwordItem();
            case "handbow" -> new HandBowItem();
            case "hunterhat" -> new HunterClothingItem(Piece.HAT, Upgrade.PLAIN);
            case "huntercoat" -> new HunterClothingItem(Piece.COAT, Upgrade.PLAIN);
            case "hunterlegs" -> new HunterClothingItem(Piece.LEGS, Upgrade.PLAIN);
            case "hunterboots" -> new HunterClothingItem(Piece.BOOTS, Upgrade.PLAIN);
            case "hunterhatsilvered" -> new HunterClothingItem(Piece.HAT, Upgrade.SILVERED);
            case "huntercoatsilvered" -> new HunterClothingItem(Piece.COAT, Upgrade.SILVERED);
            case "hunterlegssilvered" -> new HunterClothingItem(Piece.LEGS, Upgrade.SILVERED);
            case "hunterbootssilvered" -> new HunterClothingItem(Piece.BOOTS, Upgrade.SILVERED);
            case "hunterhatgarlicked" -> new HunterClothingItem(Piece.HAT, Upgrade.GARLICKED);
            case "huntercoatgarlicked" -> new HunterClothingItem(Piece.COAT, Upgrade.GARLICKED);
            case "hunterlegsgarlicked" -> new HunterClothingItem(Piece.LEGS, Upgrade.GARLICKED);
            case "hunterbootsgarlicked" -> new HunterClothingItem(Piece.BOOTS, Upgrade.GARLICKED);
            case "kobolditepickaxe" -> new PickaxeItem(Tiers.DIAMOND, 2, -2.8F, new Item.Properties()) {};
            case "quiverofmog" -> new GoblinEquipmentItem(GoblinEquipmentItem.Piece.QUIVER);
            case "gurdleofgulg" -> new GoblinEquipmentItem(GoblinEquipmentItem.Piece.GURDLE);
            case "kobolditehelm" -> new GoblinEquipmentItem(GoblinEquipmentItem.Piece.HELM);
            case "koboldite_pentacle" -> new KobolditePentacleItem();
            case "silversword" -> new GuardingSwordItem(Tiers.GOLD, 4, -2.4F, new Item.Properties()) {
                @Override
                public int getEnchantmentValue() {
                    return Tiers.IRON.getEnchantmentValue();
                }
            };
            case "vampirebook" -> new VampireBookItem();
            case "ingredient" -> new GuideBookItem.LegacyIngredient();
            case "book_collecting_fumes", "book_distilling", "book_circle_magic", "book_brews_and_infusions",
                 "book_herbology", "book_of_biomes", "book_symbology", "book_conjuration_and_fetishes",
                 "bookbiomes2", "cauldronbook" -> new GuideBookItem(GuideBookItem.Volume.fromPath(path));
            case "biomenote" -> new BiomeNoteItem();
            case "vampirehat" -> new VampireClothingItem(VampireClothingItem.Style.HAT);
            case "vampirehelmet" -> new VampireClothingItem(VampireClothingItem.Style.HELMET);
            case "vampirecoat" -> new VampireClothingItem(VampireClothingItem.Style.COAT);
            case "vampirecoat_female" -> new VampireClothingItem(VampireClothingItem.Style.FITTED_COAT);
            case "vampirechaincoat" -> new VampireClothingItem(VampireClothingItem.Style.CHAIN_COAT);
            case "vampirechaincoat_female" -> new VampireClothingItem(VampireClothingItem.Style.FITTED_CHAIN_COAT);
            case "vampirelegs" -> new VampireClothingItem(VampireClothingItem.Style.TROUSERS);
            case "vampirelegs_kilt" -> new VampireClothingItem(VampireClothingItem.Style.KILT);
            case "vampireboots" -> new VampireClothingItem(VampireClothingItem.Style.BOOTS);
            case "sungrenade" -> new GrenadeItem(false);
            case "dupgrenade" -> new GrenadeItem(true);
            case "glassgoblet" -> new GlassGobletItem();
            case "bone_needle" -> new BoneNeedleItem();
            case "mirror" -> new MirrorItem();
            case "arthana" -> new ArthanaItem();
            case "mysticbranch" -> new MysticBranchItem();
            case "mutator" -> new MutatingSprigItem();
            case "treefyd_seeds" -> new TreefydSeedsItem();
            case "broom" -> new BroomItem(false);
            case "enchanted_broom" -> new BroomItem(true);
            case "waystone", "bound_waystone", "creature_bound_waystone" -> new WaystoneItem();
            case "witchhand" -> new WitchHandItem();
            case "demon_heart" -> new DemonHeartItem(WitcheryBlocks.getRequired("demonheart").get(), true);
            case "polynesiacharm", "devilstonguecharm" -> new PolynesiaCharmItem();
            case "necromantic_stone" -> new NecromanticStoneItem();
            case "spectralstone" -> new SpectralStoneItem();
            case "deathscowl" -> new DeathClothingItem(Type.HELMET);
            case "deathsrobe" -> new DeathClothingItem(Type.CHESTPLATE);
            case "deathsfeet" -> new DeathClothingItem(Type.BOOTS);
            case "deathshand" -> new DeathHandItem();
            case "leonardsurn" -> new LeonardsUrnItem();
            case "binky_head" -> new BinkyHeadItem();
            case "attuned_stone" -> new SpiritOfferingItem(DreamSpirit.Kind.GUIDE);
            case "subdued_spirit" -> new SpiritOfferingItem(DreamSpirit.Kind.SPIRIT);
            case "subdued_village_spirit" -> new SpiritOfferingItem(DreamSpirit.Kind.VILLAGE);
            case "wormy_apple" -> DrinkableBrewItem.wormyApple();
            case "sleeping_apple" -> new SleepingAppleItem();
            case "congealed_spirit" -> new DrinkableBrewItem(Rarity.UNCOMMON, () -> MobEffects.NIGHT_VISION, 600, 1);
            case "brew_of_ink" -> new ThrowableBrewItem(BrewImpacts::splashInk);
            case "brew_of_vines" -> new ThrowableBrewItem(BrewTerrain::vines);
            case "brew_of_webs" -> new ThrowableBrewItem(BrewTerrain::webs);
            case "brew_of_thorns" -> new ThrowableBrewItem(BrewTerrain::thorns);
            case "brew_of_sprouting" -> new ThrowableBrewItem(BrewTerrain::sprouting);
            case "brew_of_erosion" -> new ThrowableBrewItem(BrewErosion::impact);
            case "brew_of_ice" -> new ThrowableBrewItem(BrewIce::impact);
            case "brew_of_love" -> new ThrowableBrewItem(BrewImpacts::splashLove);
            case "brew_of_revealing" -> new ThrowableBrewItem(BrewImpacts::splashRevealing);
            case "brew_of_raising" -> new ThrowableBrewItem(BrewImpacts::raiseDead);
            case "brew_of_frogs_tongue" -> new ThrowableBrewItem(BrewCreatureEffects::frogsTongue);
            case "brew_of_cursed_leaping" -> new ThrowableBrewItem(BrewCreatureEffects::cursedLeaping);
            case "brew_of_hitchcock" -> new ThrowableBrewItem(BrewCreatureEffects::hitchcock);
            case "brew_of_infection" -> new ThrowableBrewItem(BrewCreatureEffects::infection);
            case "quicklime" -> new QuicklimeItem();
            case "brew_of_bats" -> new ThrowableBrewItem(BrewCreatureEffects::bats);
            case "brew_of_wasting" -> new ThrowableBrewItem(BrewCreatureEffects::wasting);
            case "brew_of_grotesque" -> new DrinkableBrewItem(Rarity.UNCOMMON, GrotesqueEffect::drinkBrew);
            case "brew_of_the_depths" -> new DrinkableBrewItem(Rarity.UNCOMMON, BrewOfTheDepths::drink);
            case "brew_of_sleeping" -> new DrinkableBrewItem(Rarity.UNCOMMON, DreamTravel::requestSleep);
            case "brew_of_flowing_spirit" -> new ThrowableBrewItem(BrewImpacts::depositSpirit);
            case "brew_of_hollow_tears" -> new ThrowableBrewItem(BrewImpacts::depositHollowTears);
            case "brew_of_solid_rock" -> new ThrowableBrewItem((brew, hit) -> BrewSolidification.impact(brew, hit, Blocks.STONE));
            case "brew_of_solid_dirt" -> new ThrowableBrewItem((brew, hit) -> BrewSolidification.impact(brew, hit, Blocks.DIRT));
            case "brew_of_solid_sand" -> new ThrowableBrewItem((brew, hit) -> BrewSolidification.impact(brew, hit, Blocks.SAND));
            case "brew_of_solid_sandstone" -> new ThrowableBrewItem((brew, hit) -> BrewSolidification.impact(brew, hit, Blocks.SANDSTONE));
            case "brew_of_solid_erosion" -> new ThrowableBrewItem((brew, hit) -> BrewSolidification.impact(brew, hit, Blocks.AIR));
            case "brew_of_substitution" -> new ThrowableBrewItem(BrewSubstitution::impact);
            case "brew_of_soul_hunger" -> new DrinkableBrewItem(Rarity.UNCOMMON, SpellKnowledge.CARNOSA_DIEM::learn);
            case "brew_of_soul_anguish" -> new DrinkableBrewItem(Rarity.UNCOMMON, SpellKnowledge.IGNIANIMA::learn);
            case "brew_of_soul_fear" -> new DrinkableBrewItem(Rarity.UNCOMMON, SpellKnowledge.MORSMORDRE::learn);
            case "brew_of_soul_torment" -> new DrinkableBrewItem(Rarity.UNCOMMON, SpellKnowledge.TORMENTUM::learn);
            case "contract_of_ownership" -> new OwnershipContractItem();
            case "contract_of_torment" -> new TormentContractItem();
            case "contract_living_flame" -> new ImpSpellContractItem(ImpSpellContractItem.Spell.LIVING_FLAME);
            case "contract_fiery_tolerance" -> new ImpSpellContractItem(ImpSpellContractItem.Spell.FIERY_TOLERANCE);
            case "contract_evaporation" -> new ImpSpellContractItem(ImpSpellContractItem.Spell.EVAPORATION);
            case "contract_fiery_touch" -> new ImpSpellContractItem(ImpSpellContractItem.Spell.FIERY_TOUCH);
            case "contract_melting_touch" -> new ImpSpellContractItem(ImpSpellContractItem.Spell.MELTING_TOUCH);
            case "fire_protection_poppet", "earth_protection_poppet", "water_protection_poppet",
                    "hunger_protection_poppet", "tool_protection_poppet", "armor_protection_poppet" -> new BoundPoppetItem();
            case "death_protection_poppet" -> new DeathProtectionPoppetItem();
            case "voodoo_poppet" -> new VoodooPoppetItem();
            case "voodoo_protection_poppet" -> new BoundPoppetItem();
            case "vampiric_poppet" -> new VampiricPoppetItem();
            case "poppet_protection" -> new PoppetProtectionItem();
            case "poppet" -> new Item(new Item.Properties().stacksTo(1));
            case "odd_porkchop_raw" -> new IngredientFoodItem(new FoodProperties.Builder().nutrition(3).saturationMod(0.3F).build());
            case "odd_porkchop_cooked" -> new IngredientFoodItem(new FoodProperties.Builder().nutrition(8).saturationMod(0.8F).build());
            case "brew.fuel" -> new BrewFuelItem();
            case "brew.water" -> new EndlessWaterBrewItem();
            case "playercompass" -> new PlayerCompassItem();
            case "shelfcompass" -> new PoppetShelfCompassItem();
            case "redstone_soup" -> new DrinkableBrewItem(Rarity.UNCOMMON, () -> MobEffects.HEALTH_BOOST, 2400, 1);
            case "flying_ointment" -> new DrinkableBrewItem(Rarity.RARE, () -> MobEffects.POISON, 1200, 2);
            case "infused_brew_base" -> new DrinkableBrewItem(Rarity.RARE, () -> MobEffects.WITHER, 200, 3);
            case "brew_of_soaring" -> new DrinkableBrewItem(Rarity.RARE, InfusedBrews::applySoaring);
            case "brew_of_the_grave" -> new DrinkableBrewItem(Rarity.RARE, InfusedBrews::applyGrave);
            case "ghost_of_the_light", "soul_of_the_world", "spirit_of_otherwhere" ->
                    new DrinkableBrewItem(Rarity.RARE, () -> MobEffects.POISON, 1200, 1);
            case "infernal_animus" -> new DrinkableBrewItem(Rarity.RARE, drinker -> {
                drinker.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 1));
                drinker.addEffect(new MobEffectInstance(MobEffects.WITHER, 3600, 2));
            });
            case "happenstance_oil" -> new DrinkableBrewItem(Rarity.UNCOMMON, () -> MobEffects.NIGHT_VISION, 1200, 0);
            case "mystic_unguent" -> new DrinkableBrewItem(Rarity.RARE, () -> MobEffects.WEAKNESS, 1200, 1);
            case "seer_stone" -> new SeerStoneItem();
            case "purified_milk" -> new DrinkableBrewItem(Rarity.UNCOMMON, BrewStatusActions::drinkPurifiedMilk);
            case "warm_blood" -> new DrinkableBrewItem(Rarity.UNCOMMON, Vampirism::drinkWarmBlood);
            case "liliths_blood" -> new DrinkableBrewItem(Rarity.RARE, Vampirism::drinkLilithsBlood);
            case "icy_needle" -> new IcyNeedleItem();
            case "frozen_heart" -> DrinkableBrewItem.frozenHeart();
            case "dreamweave_of_fleet_feet" -> dreamWeave(DreamWeave.MOVEMENT);
            case "dreamweave_of_the_iron_arm" -> dreamWeave(DreamWeave.MINING);
            case "dreamweave_of_fasting" -> dreamWeave(DreamWeave.HUNGER);
            case "dreamweave_of_nightmares" -> dreamWeave(DreamWeave.NIGHTMARES);
            case "dreamweave_of_intensity" -> dreamWeave(DreamWeave.INTENSITY);
            case "filled_chalice" -> new FilledChaliceItem(WitcheryBlocks.getRequired("chalice").get());
            case "chalice", "candelabra" -> createBlockItem(path);
            case "mutandis", "mutandis_extremis" -> new MutandisItem(new Item.Properties(), path.equals("mutandis_extremis"));
            case "boline" -> new BolineItem();
            case "brewbag" -> new BrewBagItem();
            case "circletalisman" -> new CircleTalismanItem();
            case "chalkritual" -> new RitualChalkItem("circleglyphritual");
            case "chalkotherwhere" -> new RitualChalkItem("circleglyphotherwhere");
            case "chalkinfernal" -> new RitualChalkItem("circleglyphinfernal");
            case "chalkheart" -> new RitualChalkItem("circle");
            case "rowan_berries" -> new IngredientFoodItem(new FoodProperties.Builder().nutrition(1).saturationMod(6.0F).build());
            case "globe_artichoke" -> new ArtichokeItem();
            case "creeper_heart" -> DrinkableBrewItem.creeperHeart();
            case "earmuffs" -> new EarmuffsItem();
            case "witchhat" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.WITCH_HAT);
            case "witchrobe" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.WITCH_ROBE);
            case "necromancerrobe" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.NECROMANCER_ROBE);
            case "babashat" -> new WitchClothingItem(WitchClothingItem.Style.BABA_HAT);
            case "bitingbelt" -> new BitingBeltItem();
            case "barkbelt" -> new BarkBeltItem();
            case "iceslippers" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.ICY_SLIPPERS);
            case "seepingshoes" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.SEEPING_SHOES);
            case "rubyslippers" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.RUBY_SLIPPERS);
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

        SpawnEgg definition = ORIGINAL_SPAWN_EGGS.getOrDefault(path, COMPATIBILITY_EGG);
        RegistryObject<Item> egg = ITEMS.register(eggPath,
                () -> new ForgeSpawnEggItem(entityType, definition.shell(), definition.spots(), new Item.Properties()));
        LEGACY_SPAWN_EGGS.put(eggPath, egg);
    }

    private static void registerLegacyBlockItem(String legacyName) {
        String path = RegistryNameUtil.normalizePath(legacyName);

        if (LEGACY_ITEMS.containsKey(path) || LEGACY_BLOCK_ITEMS.containsKey(path)) {
            return;
        }

        RegistryObject<Item> item = ITEMS.register(path, () -> path.equals("mirrorblock") ? new MirrorItem() : createBlockItem(path));
        LEGACY_BLOCK_ITEMS.put(path, item);
    }

    private static BlockItem createBlockItem(String path) {
        Block block = WitcheryBlocks.getRequired(path).get();
        switch (path) {
            case "snowslab", "snowdoubleslab" -> {
                return new SnowSlabItem(block, new Item.Properties());
            }
            case "alluringskull" -> {
                return new BlockItem(block, new Item.Properties().stacksTo(1));
            }
            case "lilypad" -> {
                return new PlaceOnWaterBlockItem(block, new Item.Properties());
            }
            case "leapinglily" -> {
                return new LeapingLilyBlock.Item(block);
            }
            case "stockade" -> {
                return new StockadeBlock.Item(block);
            }
            case "beartrap", "wolftrap" -> {
                return new HuntingTrapItem(block);
            }
            case "wolfaltar" -> {
                return new WolfAltarItem(block);
            }
            case "brazier" -> {
                return new BlockEntityDisplayItem(block, WitcheryBlockEntities.BRAZIER);
            }
            case "crystalball" -> {
                return new BlockEntityDisplayItem(block, WitcheryBlockEntities.CRYSTAL_BALL);
            }
            case "grassper" -> {
                return new BlockEntityDisplayItem(block, WitcheryBlockEntities.GRASSPER);
            }
            case "silvervat" -> {
                return new BlockEntityDisplayItem(block, WitcheryBlockEntities.SILVER_VAT);
            }
            case "spinningwheel" -> {
                return new BlockEntityDisplayItem(block, WitcheryBlockEntities.SPINNING_WHEEL);
            }
            case "kettle" -> {
                return new BlockEntityDisplayItem(block, WitcheryBlockEntities.KETTLE);
            }
            case "distilleryidle", "distilleryburning" -> {
                return new BlockEntityDisplayItem(block, WitcheryBlockEntities.DISTILLERY);
            }
            case "statuegoddess" -> {
                return new StatueGoddessItem(block);
            }
            case "decursedirected", "decurseteleport" -> {
                return new AreaWardItem(block);
            }
            case "statueofworship" -> {
                return new StatueOfWorshipBlock.Item(block);
            }
            case "scarecrow", "trent", "witchsladder" -> {
                return new FetishItem(block);
            }
            case "demonheart" -> {
                return new DemonHeartItem(block, false);
            }
            case "bramble" -> {
                return new BrambleBlock.Item(block);
            }
            case "plantmine" -> {
                return new PlantMineBlock.Item(block);
            }
            case "shadedglass", "shadedglass_active" -> {
                return new ShadedGlassBlock.Item(block);
            }
            case "wickerbundle" -> {
                return new WickerBundleBlock.Item(block);
            }
            case "leechchest" -> {
                return new LeechChestItem(block);
            }
            case "witchsapling", "witchleaves", "witchlog", "witchwood", "witchwoodslab", "witchwooddoubleslab" -> {
                return new WitchWoodBlockItem(block, new Item.Properties());
            }
        }
        return block instanceof DoorBlock
                ? new DoubleHighBlockItem(block, new Item.Properties())
                : new BlockItem(block, new Item.Properties());
    }

    private static DreamWeaveItem dreamWeave(DreamWeave weave) {
        return new DreamWeaveItem(WitcheryBlocks.getRequired("dreamcatcher").get(), weave);
    }
}
