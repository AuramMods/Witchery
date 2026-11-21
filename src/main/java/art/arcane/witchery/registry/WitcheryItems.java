package art.arcane.witchery.registry;

import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.item.SpiritOfferingItem;
import art.arcane.witchery.item.SpectralStoneItem;

import art.arcane.witchery.item.BrewFuelItem;
import art.arcane.witchery.item.EndlessWaterBrewItem;
import art.arcane.witchery.item.PlayerCompassItem;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.brewing.BrewImpacts;
import art.arcane.witchery.brewing.BrewCreatureEffects;
import art.arcane.witchery.brewing.BrewTerrain;
import art.arcane.witchery.item.AnointingPasteItem;
import art.arcane.witchery.item.BoundPoppetItem;
import art.arcane.witchery.item.ArthanaItem;
import art.arcane.witchery.item.BolineItem;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.BroomItem;
import art.arcane.witchery.item.CaneSwordItem;
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
import art.arcane.witchery.item.DeathHandItem;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.DeathProtectionPoppetItem;
import art.arcane.witchery.item.FetishItem;
import art.arcane.witchery.item.DrinkableBrewItem;
import art.arcane.witchery.item.DreamWeaveItem;
import art.arcane.witchery.item.FilledChaliceItem;
import art.arcane.witchery.item.GlassGobletItem;
import art.arcane.witchery.item.BoneNeedleItem;
import art.arcane.witchery.item.IcyNeedleItem;
import art.arcane.witchery.item.LeechChestItem;
import art.arcane.witchery.item.MirrorItem;
import art.arcane.witchery.item.MoonCharmItem;
import art.arcane.witchery.item.WolfAltarItem;
import art.arcane.witchery.item.WolfsbaneFlowerItem;
import art.arcane.witchery.item.OwnershipContractItem;
import art.arcane.witchery.item.MutandisItem;
import art.arcane.witchery.item.MysticBranchItem;
import art.arcane.witchery.item.NecromanticStoneItem;
import art.arcane.witchery.item.RitualChalkItem;
import art.arcane.witchery.item.SeerStoneItem;
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
import art.arcane.witchery.item.WitchHandItem;
import art.arcane.witchery.item.WitchWoodBlockItem;
import art.arcane.witchery.item.WitchSeedsItem;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.infusion.InfusedBrews;
import art.arcane.witchery.infusion.SpellKnowledge;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
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
                "bolt_spawn_egg", "droplet_spawn_egg")) {
            LEGACY_SPAWN_EGGS.put(name, ITEMS.register(name, () -> new Item(new Item.Properties())));
        }
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
            case "brewbottle" -> new BrewBottleItem();
            case "anointing_paste" -> new AnointingPasteItem(new Item.Properties());
            case "taglockkit" -> new TaglockItem();
            case "coffin" -> createBlockItem("coffinblock");
            case "mooncharm" -> new MoonCharmItem();
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
            case "kobolditepickaxe" -> new PickaxeItem(Tiers.DIAMOND, 1, -2.8F, new Item.Properties()) {};
            case "silversword" -> new SwordItem(Tiers.GOLD, 4, -2.4F, new Item.Properties()) {
                @Override
                public int getEnchantmentValue() {
                    return Tiers.IRON.getEnchantmentValue();
                }
            };
            case "vampirebook" -> new VampireBookItem();
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
            case "broom" -> new BroomItem(false);
            case "enchanted_broom" -> new BroomItem(true);
            case "waystone", "bound_waystone", "creature_bound_waystone" -> new WaystoneItem();
            case "witchhand" -> new WitchHandItem();
            case "demon_heart" -> new DemonHeartItem(WitcheryBlocks.getRequired("demonheart").get(), true);
            case "devilstonguecharm" -> new Item(new Item.Properties().durability(50).rarity(Rarity.UNCOMMON));
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
            case "wormy_apple" -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEat()
                    .effect(() -> new MobEffectInstance(MobEffects.POISON, 60), 1).build()));
            case "congealed_spirit" -> new DrinkableBrewItem(() -> MobEffects.NIGHT_VISION, 600, 1);
            case "brew_of_ink" -> new ThrowableBrewItem(BrewImpacts::splashInk);
            case "brew_of_vines" -> new ThrowableBrewItem(BrewTerrain::vines);
            case "brew_of_webs" -> new ThrowableBrewItem(BrewTerrain::webs);
            case "brew_of_thorns" -> new ThrowableBrewItem(BrewTerrain::thorns);
            case "brew_of_sprouting" -> new ThrowableBrewItem(BrewTerrain::sprouting);
            case "brew_of_love" -> new ThrowableBrewItem(BrewImpacts::splashLove);
            case "brew_of_revealing" -> new ThrowableBrewItem(BrewImpacts::splashRevealing);
            case "brew_of_raising" -> new ThrowableBrewItem(BrewImpacts::raiseDead);
            case "brew_of_frogs_tongue" -> new ThrowableBrewItem(BrewCreatureEffects::frogsTongue);
            case "brew_of_cursed_leaping" -> new ThrowableBrewItem(BrewCreatureEffects::cursedLeaping);
            case "brew_of_hitchcock" -> new ThrowableBrewItem(BrewCreatureEffects::hitchcock);
            case "brew_of_bats" -> new ThrowableBrewItem(BrewCreatureEffects::bats);
            case "brew_of_wasting" -> new ThrowableBrewItem(BrewCreatureEffects::wasting);
            case "brew_of_grotesque" -> new DrinkableBrewItem(drinker ->
                    drinker.addEffect(new MobEffectInstance(WitcheryEffects.GROTESQUE.get(), 4_800, 0, false, false, false)));
            case "brew_of_the_depths" -> new DrinkableBrewItem(WitcheryEffects.DEPTHS, 6000, 0);
            case "brew_of_sleeping" -> new DrinkableBrewItem(DreamTravel::requestSleep);
            case "brew_of_flowing_spirit" -> new ThrowableBrewItem(BrewImpacts::depositSpirit);
            case "brew_of_hollow_tears" -> new ThrowableBrewItem(BrewImpacts::depositHollowTears);
            case "brew_of_soul_hunger" -> new DrinkableBrewItem(SpellKnowledge.CARNOSA_DIEM::learn);
            case "brew_of_soul_anguish" -> new DrinkableBrewItem(SpellKnowledge.IGNIANIMA::learn);
            case "brew_of_soul_fear" -> new DrinkableBrewItem(SpellKnowledge.MORSMORDRE::learn);
            case "brew_of_soul_torment" -> new DrinkableBrewItem(SpellKnowledge.TORMENTUM::learn);
            case "contract_of_ownership" -> new OwnershipContractItem();
            case "contract_of_torment" -> new TormentContractItem();
            case "fire_protection_poppet", "earth_protection_poppet", "water_protection_poppet",
                    "hunger_protection_poppet", "tool_protection_poppet", "armor_protection_poppet" -> new BoundPoppetItem();
            case "death_protection_poppet" -> new DeathProtectionPoppetItem();
            case "voodoo_poppet" -> new VoodooPoppetItem();
            case "voodoo_protection_poppet" -> new BoundPoppetItem();
            case "vampiric_poppet" -> new VampiricPoppetItem();
            case "poppet_protection" -> new PoppetProtectionItem();
            case "poppet" -> new Item(new Item.Properties().stacksTo(1));
            case "odd_porkchop_raw" -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(3).saturationMod(0.3F).alwaysEat().build()));
            case "odd_porkchop_cooked" -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(8).saturationMod(0.8F).alwaysEat().build()));
            case "brew.fuel" -> new BrewFuelItem();
            case "brew.water" -> new EndlessWaterBrewItem();
            case "playercompass" -> new PlayerCompassItem();
            case "redstone_soup" -> new DrinkableBrewItem(() -> MobEffects.HEALTH_BOOST, 2400, 1);
            case "flying_ointment" -> new DrinkableBrewItem(() -> MobEffects.POISON, 1200, 2);
            case "infused_brew_base" -> new DrinkableBrewItem(() -> MobEffects.WITHER, 200, 3);
            case "brew_of_soaring" -> new DrinkableBrewItem(InfusedBrews::applySoaring);
            case "brew_of_the_grave" -> new DrinkableBrewItem(InfusedBrews::applyGrave);
            case "ghost_of_the_light", "soul_of_the_world", "spirit_of_otherwhere" ->
                    new DrinkableBrewItem(() -> MobEffects.POISON, 1200, 1);
            case "infernal_animus" -> new DrinkableBrewItem(drinker -> {
                drinker.addEffect(new MobEffectInstance(MobEffects.POISON, 1200, 1));
                drinker.addEffect(new MobEffectInstance(MobEffects.WITHER, 3600, 2));
            });
            case "happenstance_oil" -> new DrinkableBrewItem(() -> MobEffects.NIGHT_VISION, 1200, 0);
            case "mystic_unguent" -> new DrinkableBrewItem(() -> MobEffects.WEAKNESS, 1200, 1);
            case "seer_stone" -> new SeerStoneItem();
            case "purified_milk" -> new DrinkableBrewItem(DrinkableBrewItem::purifyMilk);
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
            case "chalkritual" -> new RitualChalkItem("circleglyphritual");
            case "chalkotherwhere" -> new RitualChalkItem("circleglyphotherwhere");
            case "chalkinfernal" -> new RitualChalkItem("circleglyphinfernal");
            case "chalkheart" -> new RitualChalkItem("circle");
            case "rowan_berries" -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(1).saturationMod(6.0F).build()));
            case "globe_artichoke" -> new Item(new Item.Properties()
                    .food(new FoodProperties.Builder().nutrition(20).saturationMod(0F).build()));
            case "creeper_heart" -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .alwaysEat().effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 20), 1.0F).build()));
            case "earmuffs" -> new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Properties());
            case "witchhat" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.WITCH_HAT);
            case "witchrobe" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.WITCH_ROBE);
            case "necromancerrobe" -> new WitchClothingItem.Dyeable(WitchClothingItem.Style.NECROMANCER_ROBE);
            case "babashat" -> new WitchClothingItem(WitchClothingItem.Style.BABA_HAT);
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
                () -> switch (path) {
                    case "covenwitch" -> new ForgeSpawnEggItem(entityType, 0x111111, 0x002D03, new Item.Properties());
                    case "witchhunter" -> new ForgeSpawnEggItem(entityType, 0x78706B, 0x231C19, new Item.Properties());
                    case "hellhound" -> new ForgeSpawnEggItem(entityType, 0xD86500, 0x5B1208, new Item.Properties());
                    case "hornedhuntsman" -> new ForgeSpawnEggItem(entityType, 11523, 4007964, new Item.Properties());
                    case "wolfman" -> new ForgeSpawnEggItem(entityType, 0x2D2D2D, 0x606060, new Item.Properties());
                    case "werevillager" -> new ForgeSpawnEggItem(entityType, 0x563C33, 0xBD8B72, new Item.Properties());
                    case "spirit" -> new ForgeSpawnEggItem(entityType, 0xFFA530, 0xEECA00, new Item.Properties());
                    default -> new ForgeSpawnEggItem(entityType, 0x6E5A4E, 0xA4A29D, new Item.Properties());
                });
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
            case "beartrap", "wolftrap" -> {
                return new HuntingTrapItem(block);
            }
            case "wolfaltar" -> {
                return new WolfAltarItem(block);
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
            case "wickerbundle" -> {
                return new WickerBundleBlock.Item(block);
            }
            case "leechchest" -> {
                return new LeechChestItem(block);
            }
            case "witchsapling", "witchleaves", "witchlog", "witchwood" -> {
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
