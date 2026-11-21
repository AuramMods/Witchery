package art.arcane.witchery.brewing;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public enum BrewDye {
    BLACK(DyeColor.BLACK, 1118481, "ink_sac"),
    RED(DyeColor.RED, 12464176),
    GREEN(DyeColor.GREEN, 5732898),
    BROWN(DyeColor.BROWN, 5057301, "cocoa_beans"),
    BLUE(DyeColor.BLUE, 2247599, "lapis_lazuli"),
    PURPLE(DyeColor.PURPLE, 8532146),
    CYAN(DyeColor.CYAN, 3968688),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY, 11513789),
    GRAY(DyeColor.GRAY, 7763574),
    PINK(DyeColor.PINK, 15574987),
    LIME(DyeColor.LIME, 8639516),
    YELLOW(DyeColor.YELLOW, 15197994),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE, 12179199),
    MAGENTA(DyeColor.MAGENTA, 14383829),
    ORANGE(DyeColor.ORANGE, 15113780),
    WHITE(DyeColor.WHITE, 16777215, "bone_meal");

    private static final BrewDye[] LEGACY_ORDER = values();
    private final DyeColor color;
    private final int rgb;
    private final String item;

    BrewDye(DyeColor color, int rgb) {
        this(color, rgb, color.getName() + "_dye");
    }

    BrewDye(DyeColor color, int rgb, String item) {
        this.color = color;
        this.rgb = rgb;
        this.item = item;
    }

    public static BrewDye byLegacyId(int id) {
        return LEGACY_ORDER[Mth.clamp(id, 0, LEGACY_ORDER.length - 1)];
    }

    String wool() { return color.getName() + "_wool"; }
    String item() { return item; }
    boolean colorsCreatures() { return item.endsWith("_dye"); }
    public int rgb() { return rgb; }
    public Component label() { return Component.translatable("color.minecraft." + color.getName()); }
}
