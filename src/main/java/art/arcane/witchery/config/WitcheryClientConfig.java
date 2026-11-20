package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WitcheryClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue GUI_ON_LEFT;
    public static final ForgeConfigSpec.BooleanValue SHOW_VAMPIRE_TARGET_BLOOD_TEXT;
    public static final ForgeConfigSpec.BooleanValue RENDER_HUNTSMAN_GLINT;
    public static final ForgeConfigSpec.DoubleValue MANTRAP_OPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        GUI_ON_LEFT = builder
                .comment("Show the infusion charge meter on the left side of the screen.")
                .define("guiOnLeft", true);
        SHOW_VAMPIRE_TARGET_BLOOD_TEXT = builder.define("hudShowVampireTargetBloodText", false);
        RENDER_HUNTSMAN_GLINT = builder.define("renderHuntsmanGlintEffect", true);
        MANTRAP_OPACITY = builder.defineInRange("mantrapOpacity", 0.3, 0.1, 1.0);
        SPEC = builder.build();
    }

    private WitcheryClientConfig() {}
}
