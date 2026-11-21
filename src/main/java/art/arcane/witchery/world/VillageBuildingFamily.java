package art.arcane.witchery.world;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public enum VillageBuildingFamily {
    GARDEN_HOUSE("GardenHouse"),
    HOUSE("House"),
    WOOD_HUT("WoodHut"),
    HALL("Hall"),
    HOUSE_3("House3"),
    SINGLE_FIELD("SingleField"),
    DOUBLE_FIELD("DoubleField"),
    BLACKSMITH("Blacksmith", 1, 5, 0, 1),
    CHURCH("Church", 0, 10, 0, 1);

    private final String configName;
    private final int groups;
    private final int weight;
    private final int min;
    private final int max;

    VillageBuildingFamily(String configName) {
        this(configName, 3, 20, 3, 5);
    }

    VillageBuildingFamily(String configName, int groups, int weight, int min, int max) {
        this.configName = configName;
        this.groups = groups;
        this.weight = weight;
        this.min = min;
        this.max = max;
    }

    public String configName() { return configName; }
    public int defaultGroups() { return groups; }
    public int defaultWeight() { return weight; }
    public int defaultMin() { return min; }
    public int defaultMax() { return max; }

    @Nullable
    static VillageBuildingFamily forTemplate(String style, ResourceLocation id) {
        String prefix = "village/" + style + "/houses/" + style + "_";
        if (!id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) || !id.getPath().startsWith(prefix)) return null;
        String name = id.getPath().substring(prefix.length());
        if (name.startsWith("small_house_")) {
            String garden = switch (style) {
                case "plains" -> "small_house_8";
                case "desert" -> "small_house_7";
                case "savanna" -> "small_house_4";
                case "snowy" -> "small_house_3";
                case "taiga" -> "small_house_1";
                default -> "";
            };
            return name.equals(garden) ? GARDEN_HOUSE : WOOD_HUT;
        }
        if (name.startsWith("medium_house_") || name.startsWith("big_house_")) return HOUSE_3;
        if (name.startsWith("library_")) return HOUSE;
        if (name.startsWith("butcher_shop_") || name.startsWith("butchers_shop_")) return HALL;
        // Field1 is the larger vanilla plot despite the legacy SingleField option name.
        if (name.startsWith("large_farm_") || style.equals("snowy") && name.equals("farm_2")) return SINGLE_FIELD;
        if (name.startsWith("small_farm") || name.startsWith("farm_")) return DOUBLE_FIELD;
        if (name.startsWith("weaponsmith_") || name.startsWith("weapon_smith_")) return BLACKSMITH;
        if (name.startsWith("temple_")) return CHURCH;
        return null;
    }
}
