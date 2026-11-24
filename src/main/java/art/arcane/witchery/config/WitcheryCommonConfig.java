package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WitcheryCommonConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_MOD_INTEGRATION;
    public static final ForgeConfigSpec.BooleanValue SMELT_ALL_SAPLINGS_TO_WOOD_ASH;
    public static final ForgeConfigSpec.BooleanValue ALLOW_GODDESS_STATUE_RECIPE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_VOID_BRAMBLE_RECIPE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ALLOW_MOD_INTEGRATION = builder
                .comment("Enable Witchery's optional integrations with other mods. Restart Minecraft or the dedicated server after changing this option.")
                .define("allowModIntegration", true);
        SMELT_ALL_SAPLINGS_TO_WOOD_ASH = builder
                .comment("Include all saplings in the wood ash recipe. Disabling this keeps the six original vanilla saplings and Rowan.")
                .define("addSmeltingForAllSaplingsToWoodAsh", true);
        ALLOW_GODDESS_STATUE_RECIPE = builder.define("allowGoddessStatueRecipe", false);
        ALLOW_VOID_BRAMBLE_RECIPE = builder.define("allowVoidBrambleRecipe", false);
        SPEC = builder.build();
    }

    private WitcheryCommonConfig() {
    }
}
