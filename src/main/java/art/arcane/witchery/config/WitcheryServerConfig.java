package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public final class WitcheryServerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_DEATHS_HOOD_TO_FREEZE_VICTIMS;
    public static final ForgeConfigSpec.BooleanValue DOUBLE_FUME_FILTER_CHANCE;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_TAGLOCK_COLLECTION_ON_NON_PVP_SERVERS;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_TAGLOCK_COLLECTION_FOR_OPS;
    public static final ForgeConfigSpec.IntValue COVEN_WITCH_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_POPPET_SHELVES;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VAMPIRE_RITUAL;
    public static final ForgeConfigSpec.BooleanValue ALLOW_DEATH_ITEM_RECOVERY_RITE;
    public static final ForgeConfigSpec.BooleanValue RESPECT_OTHER_DEATH_CHEST_MODS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VAMPIRE_QUESTS;
    public static final ForgeConfigSpec.IntValue VAMPIRE_DEATH_ITEM_KEEP_ALIVE_MINS;
    public static final ForgeConfigSpec.DoubleValue VAMPIRE_HUNTER_SPAWN_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VAMPIRE_WEREWOLF_HYBRIDS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PLAYER_TO_PLAYER_WOLF_INFECTION;
    public static final ForgeConfigSpec.BooleanValue DECURSE_DIRECTED_ENABLED;
    public static final ForgeConfigSpec.BooleanValue DECURSE_TELEPORT_ENABLED;
    public static final ForgeConfigSpec.IntValue DECURSE_DIRECTED_RADIUS;
    public static final ForgeConfigSpec.IntValue DECURSE_TELEPORT_RADIUS;
    public static final ForgeConfigSpec.BooleanValue SHRINK_MIRROR_WORLD;
    public static final ForgeConfigSpec.DoubleValue FAIREST_SPAWN_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_CHAT_MASQUERADING;
    public static final ForgeConfigSpec.BooleanValue ALLOW_NAMEPLATE_MASQUERADING;
    public static final ForgeConfigSpec.BooleanValue ALLOW_BIOME_MODIFICATION_RITUALS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_HELL_ON_EARTH_FIRES;
    public static final ForgeConfigSpec.IntValue RITE_OF_ECLIPSE_COOLDOWN_SECONDS;
    public static final ForgeConfigSpec.BooleanValue GENERATE_WICKER_MEN;
    public static final ForgeConfigSpec.BooleanValue GENERATE_COVENS;
    public static final ForgeConfigSpec.BooleanValue GENERATE_SHACKS;
    public static final ForgeConfigSpec.BooleanValue GENERATE_HOBGOBLIN_HUTS;
    public static final ForgeConfigSpec.BooleanValue GENERATE_BOOK_SHOPS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TOWN_BOOKSHOP_ALLOWED_BOOKS;
    public static final ForgeConfigSpec.BooleanValue HOBGOBLIN_DESPAWN_BLOCK;
    public static final ForgeConfigSpec.IntValue HOBGOBLIN_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.IntValue HOBGOBLIN_GOD_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue WORLD_GEN_FREQUENCY;
    public static final ForgeConfigSpec.BooleanValue WORLD_GEN_TWILIGHT_FOREST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> STRAWMAN_SPAWNER_RULES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        DOUBLE_FUME_FILTER_CHANCE = builder
                .comment("Increase the chance bonus from an aligned filtered fume funnel from 30% to 80%.")
                .define("doubleFumeFilterChance", false);
        RESTRICT_TAGLOCK_COLLECTION_ON_NON_PVP_SERVERS = builder
                .comment("Block collecting other players' taglocks while server PvP is disabled.")
                .define("restrictTaglockCollectionOnNonPvpServers", false);
        RESTRICT_TAGLOCK_COLLECTION_FOR_OPS = builder
                .comment("Block collecting taglocks from server operators.")
                .define("restrictTaglockCollectionForOps", false);
        COVEN_WITCH_SPAWN_WEIGHT = builder
                .comment("One in this many otherwise suitable spawn attempts produces a coven witch.")
                .defineInRange("covenWitchSpawnWeight", 2, 1, Integer.MAX_VALUE);
        RESTRICT_POPPET_SHELVES = builder
                .comment("Limit Poppet Shelf protection to the Overworld, Nether, End, and Dream.")
                .define("restrictPoppetShelvesToVanillaAndSpiritDimensions", true);
        ALLOW_VAMPIRE_RITUAL = builder.define("allowVampireRitual", true);
        ALLOW_DEATH_ITEM_RECOVERY_RITE = builder
                .comment("Allow Prior Incarnation to recover expired player death drops.")
                .define("allowDeathItemRecoveryRite", true);
        RESPECT_OTHER_DEATH_CHEST_MODS = builder
                .comment("Yield death storage to Corail Tombstone, GraveStone Mod, Corpse, or You're in Grave Danger.")
                .define("respectOtherDeathChestMods", true);
        ALLOW_VAMPIRE_QUESTS = builder.define("allowVampireQuests", true);
        VAMPIRE_DEATH_ITEM_KEEP_ALIVE_MINS = builder
                .comment("Minutes before items dropped by a vampire player on death despawn.")
                .defineInRange("vampireDeathItemKeepAliveMins", 12, 5, 30);
        VAMPIRE_HUNTER_SPAWN_CHANCE = builder
                .comment("Chance every five seconds that a village reports a disliked vampire at rank ten.")
                .defineInRange("vampireHunterSpawnChance", 0.01, 0.0, 1.0);
        ALLOW_VAMPIRE_WEREWOLF_HYBRIDS = builder.define("allowVampireWerewolfHybrids", true);
        ALLOW_PLAYER_TO_PLAYER_WOLF_INFECTION = builder.define("allowPlayerToPlayerWolfInfection", true);
        DECURSE_DIRECTED_ENABLED = builder.define("decurseDirectedEnabled", false);
        DECURSE_TELEPORT_ENABLED = builder.define("decurseTeleportPullEnabled", false);
        DECURSE_DIRECTED_RADIUS = builder
                .comment("Horizontal range of a Statue of Broken Curses, in blocks.")
                .defineInRange("decurseDirectedProtectRadius", 32, 0, 128);
        DECURSE_TELEPORT_RADIUS = builder
                .comment("Horizontal range of a Statue of Occluded Summons, in blocks.")
                .defineInRange("decurseTeleportPullProtectRadius", 32, 0, 128);
        ALLOW_DEATHS_HOOD_TO_FREEZE_VICTIMS = builder.define("allowDeathsHoodToFreezeVictims", true);
        SHRINK_MIRROR_WORLD = builder
                .comment("Generate eight chamber tiers in new Mirror chunks. Existing chambers and links are preserved.")
                .worldRestart()
                .define("shrinkMirrorWorld", false);
        FAIREST_SPAWN_CHANCE = builder
                .comment("Chance that a Mirror introduces a new Fairest during an eligible conversation.")
                .defineInRange("newFairestOfThemAllSpawnChance", 0.01, 0.01, 1.0);
        ALLOW_CHAT_MASQUERADING = builder.define("allowChatMasquerading", true);
        ALLOW_NAMEPLATE_MASQUERADING = builder.define("allowNameplateMasquerading", true);
        ALLOW_BIOME_MODIFICATION_RITUALS = builder
                .comment("Allow the Rite of Climate Change to edit saved chunk biomes.")
                .define("allowBiomeModificationRituals", true);
        ALLOW_HELL_ON_EARTH_FIRES = builder
                .comment("Allow an active cat familiar to make the Hell on Earth rite ignite plants.")
                .define("allowHellOnEarthFires", true);
        RITE_OF_ECLIPSE_COOLDOWN_SECONDS = builder
                .comment("Seconds between eclipses in a dimension with more than one player. Zero allows consecutive casts.")
                .defineInRange("riteOfEclipseCooldownInSecs", 0, 0, 3600);
        GENERATE_WICKER_MEN = builder
                .comment("Generate Wicker Men in new chunks.")
                .worldRestart().define("generateWickerMen", true);
        GENERATE_COVENS = builder.comment("Generate coven circles in new chunks.")
                .worldRestart().define("generateCovens", true);
        GENERATE_SHACKS = builder.comment("Generate witches' shacks in new chunks.")
                .worldRestart().define("generateShacks", true);
        GENERATE_HOBGOBLIN_HUTS = builder.comment("Generate hobgoblin huts in new chunks.")
                .worldRestart().define("generateHobgoblinHuts", true);
        GENERATE_BOOK_SHOPS = builder.comment("Add Witchery bookshops to newly generated villages.")
                .worldRestart().define("generateBookShops", true);
        TOWN_BOOKSHOP_ALLOWED_BOOKS = builder.comment("Extra bookshop stock as item IDs or legacy item IDs followed by comma and metadata. Repeat entries for additional weight.")
                .defineListAllowEmpty("townBookshopAllowedBooks", List.of("minecraft:book", "witchery:ingredient,46",
                        "witchery:ingredient,47", "witchery:ingredient,48", "witchery:ingredient,49",
                        "witchery:ingredient,81", "witchery:ingredient,106", "witchery:ingredient,107",
                        "witchery:ingredient,127", "witchery:bookbiomes2", "witchery:cauldronbook",
                        "thaumcraft:thaumonomicon", "tconstruct:materials_and_you", "tconstruct:puny_smelting",
                        "tconstruct:mighty_smelting", "tconstruct:tinkers_gadgetry"), entry -> entry instanceof String);
        HOBGOBLIN_DESPAWN_BLOCK = builder.define("hobgoblinDespawnBlock", true);
        HOBGOBLIN_SPAWN_WEIGHT = builder.comment("Wild hobgoblin spawn weight in forests and swamps. Plains use one less, with a minimum of one.")
                .worldRestart().defineInRange("hobgoblinSpawnWeight", 4, 1, Integer.MAX_VALUE);
        HOBGOBLIN_GOD_SPAWN_CHANCE = builder.comment("Chance out of 10,000 for a statue with fifteen worshippers to summon Mog and Gulg each pulse.")
                .defineInRange("hobgoblinGodSpawnChance", 10, 0, 100);
        WORLD_GEN_FREQUENCY = builder
                .comment("Witchery surface structures use regions this many chunks wide plus eight.")
                .worldRestart().defineInRange("worldGenFrequency", 12, 1, 64);
        WORLD_GEN_TWILIGHT_FOREST = builder
                .comment("Allow Witchery surface structures in the Twilight Forest when installed.")
                .worldRestart().define("worldGenInTwilightForest", true);
        STRAWMAN_SPAWNER_RULES = builder
                .comment("Entity IDs for new Wicker Man spawners. Repeat an entry to make it more likely.")
                .worldRestart().defineListAllowEmpty("strawmanSpawnerRules",
                        List.of("minecraft:zombie", "minecraft:zombie", "minecraft:skeleton"), entry -> entry instanceof String);
        SPEC = builder.build();
    }

    private WitcheryServerConfig() {
    }
}
