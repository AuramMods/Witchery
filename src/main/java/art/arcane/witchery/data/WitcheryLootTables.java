package art.arcane.witchery.data;

import art.arcane.witchery.Witchery;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public final class WitcheryLootTables {
    public static final ResourceLocation MINESHAFT_CORRIDOR =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/mineshaft_corridor");
    public static final ResourceLocation WITCH_SHACK =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/witch_shack");
    public static final Set<ResourceLocation> LEGACY_STRUCTURE_CHESTS = Set.of(WITCH_SHACK,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/village_apothecary"),
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/village/keep"),
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/village/watchtower"));

    private WitcheryLootTables() {}
}
