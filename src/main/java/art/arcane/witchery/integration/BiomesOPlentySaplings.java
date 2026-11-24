package art.arcane.witchery.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class BiomesOPlentySaplings {
    public static final ResourceLocation FIR = ResourceLocation.fromNamespaceAndPath("biomesoplenty", "fir_sapling");
    public static final int LEGACY_FIR_METADATA = 6;

    private BiomesOPlentySaplings() {}

    public static boolean isFir(ItemStack stack) {
        return FIR.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    public static boolean isLegacySaplings(String id) {
        return "BiomesOPlenty:saplings".equalsIgnoreCase(id) || "BiomesOPlenty:bop.saplings".equalsIgnoreCase(id);
    }
}
