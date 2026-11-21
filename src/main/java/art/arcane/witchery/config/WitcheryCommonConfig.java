package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WitcheryCommonConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue SMELT_ALL_SAPLINGS_TO_WOOD_ASH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SMELT_ALL_SAPLINGS_TO_WOOD_ASH = builder
                .comment("Include all saplings in the wood ash recipe. Disabling this keeps the six original vanilla saplings and Rowan.")
                .define("addSmeltingForAllSaplingsToWoodAsh", true);
        SPEC = builder.build();
    }

    private WitcheryCommonConfig() {
    }
}
