package art.arcane.witchery.item;

import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class BiomeNoteItem extends Item {
    public static final String BIOME = "WitcheryBiome";

    public BiomeNoteItem() {
        super(new Properties());
    }

    @Override
    public Component getName(ItemStack stack) {
        var biome = biomeId(stack);
        return biome == null ? super.getName(stack) : Component.translatable("item.witchery.biomenote.discovered",
                Component.translatable(biome.toLanguageKey("biome")));
    }

    @Nullable
    public static ResourceLocation biomeId(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(BIOME, Tag.TAG_STRING)) return null;
        String id = stack.getTag().getString(BIOME);
        return id.isEmpty() ? null : ResourceLocation.tryParse(id);
    }

    static void importPage(ItemStack stack, int page) {
        if (stack.hasTag() && stack.getTag().contains(BIOME, Tag.TAG_STRING)) return;
        var biome = LegacyBiomeAtlas.biomeAt(page);
        if (biome != null) stack.getOrCreateTag().putString(BIOME, biome.toString());
    }
}
