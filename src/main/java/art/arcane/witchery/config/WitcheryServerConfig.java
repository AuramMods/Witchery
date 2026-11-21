package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WitcheryServerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_DEATHS_HOOD_TO_FREEZE_VICTIMS;
    public static final ForgeConfigSpec.BooleanValue DOUBLE_FUME_FILTER_CHANCE;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_TAGLOCK_COLLECTION_ON_NON_PVP_SERVERS;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_TAGLOCK_COLLECTION_FOR_OPS;
    public static final ForgeConfigSpec.IntValue COVEN_WITCH_SPAWN_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_POPPET_SHELVES;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VAMPIRE_RITUAL;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VAMPIRE_QUESTS;
    public static final ForgeConfigSpec.IntValue VAMPIRE_DEATH_ITEM_KEEP_ALIVE_MINS;
    public static final ForgeConfigSpec.DoubleValue VAMPIRE_HUNTER_SPAWN_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VAMPIRE_WEREWOLF_HYBRIDS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PLAYER_TO_PLAYER_WOLF_INFECTION;
    public static final ForgeConfigSpec.BooleanValue DECURSE_DIRECTED_ENABLED;
    public static final ForgeConfigSpec.BooleanValue DECURSE_TELEPORT_ENABLED;
    public static final ForgeConfigSpec.IntValue DECURSE_DIRECTED_RADIUS;
    public static final ForgeConfigSpec.IntValue DECURSE_TELEPORT_RADIUS;

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
        SPEC = builder.build();
    }

    private WitcheryServerConfig() {
    }
}
