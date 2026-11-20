package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WitcheryClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue GUI_ON_LEFT;
    public static final ForgeConfigSpec.BooleanValue SHOW_VAMPIRE_TARGET_BLOOD_TEXT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        GUI_ON_LEFT = builder
                .comment("Show the infusion charge meter on the left side of the screen.")
                .define("guiOnLeft", true);
        SHOW_VAMPIRE_TARGET_BLOOD_TEXT = builder.define("hudShowVampireTargetBloodText", false);
        SPEC = builder.build();
    }

    private WitcheryClientConfig() {}
}
