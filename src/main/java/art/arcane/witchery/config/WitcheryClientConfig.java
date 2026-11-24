package art.arcane.witchery.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class WitcheryClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_MOD_JEI;
    public static final ForgeConfigSpec.BooleanValue GUI_ON_LEFT;
    public static final ForgeConfigSpec.BooleanValue SHOW_VAMPIRE_TARGET_BLOOD_TEXT;
    public static final ForgeConfigSpec.ConfigValue<Integer> BRANCH_GLYPH_SET;
    public static final ForgeConfigSpec.BooleanValue RENDER_3D_GLINT;
    public static final ForgeConfigSpec.BooleanValue RENDER_HUNTSMAN_GLINT;
    public static final ForgeConfigSpec.DoubleValue MANTRAP_OPACITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ALLOW_MOD_JEI = builder
                .comment("Add Witchery's machine recipes, crafting layouts, and item variants to JEI.")
                .define("allowModJEI", true);
        GUI_ON_LEFT = builder
                .comment("Show the infusion charge meter on the left side of the screen.")
                .define("guiOnLeft", true);
        SHOW_VAMPIRE_TARGET_BLOOD_TEXT = builder.define("hudShowVampireTargetBloodText", false);
        BRANCH_GLYPH_SET = builder.comment("Mystic Branch gesture glyphs. Use 1 for the alternate set. Other values use the original default.")
                .define("branchGlyphSet", 0);
        RENDER_3D_GLINT = builder.comment("Render glint on the held models of Mystic Branches, Huntsman Spears and enchanted Cane Swords.")
                .define("render3dGlintEffect", true);
        RENDER_HUNTSMAN_GLINT = builder.define("renderHuntsmanGlintEffect", true);
        MANTRAP_OPACITY = builder.defineInRange("mantrapOpacity", 0.3, 0.1, 1.0);
        SPEC = builder.build();
    }

    private WitcheryClientConfig() {}
}
