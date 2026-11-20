package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WitcheryServerConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue DOUBLE_FUME_FILTER_CHANCE;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_TAGLOCK_COLLECTION_ON_NON_PVP_SERVERS;
    public static final ForgeConfigSpec.BooleanValue RESTRICT_TAGLOCK_COLLECTION_FOR_OPS;

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
        SPEC = builder.build();
    }

    private WitcheryServerConfig() {
    }
}
