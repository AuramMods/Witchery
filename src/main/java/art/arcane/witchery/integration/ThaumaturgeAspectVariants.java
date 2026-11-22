package art.arcane.witchery.integration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.block.WitchSaplingBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.item.CircleTalismanItem;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ThaumaturgeAspectVariants {
    private static final Map<String, Integer> ALDER = Map.of("herba", 2, "perditio", 1);
    private static final Map<String, Integer> HAWTHORN = Map.of("herba", 2, "ordo", 1);
    private static final Map<String, Integer> BLOODIED_WICKER = Map.of("herba", 2, "bestia", 2, "mortuus", 3, "ignis", 1);
    private static final Map<String, Integer> INK_MINE = Map.of("aqua", 1, "perditio", 1, "tenebrae", 2, "machina", 1, "sensus", 1);
    private static final Map<String, Integer> THORNS_MINE = Map.of("aqua", 1, "perditio", 2, "herba", 1, "aversio", 1, "machina", 1, "sensus", 1);
    private static final Map<String, Integer> SPROUTING_MINE = Map.of("aqua", 1, "perditio", 1, "herba", 4, "machina", 1, "sensus", 1);
    private static final Map<String, Integer> ENDER_BRAMBLE = Map.of("ordo", 2, "alienis", 3, "aqua", 1, "herba", 1);
    private static final Map<String, Integer> EMPTY_TALISMAN = Map.of("instrumentum", 2, "vitreus", 4, "metallum", 8, "praecantatio", 3);

    private ThaumaturgeAspectVariants() {}

    @Nullable
    public static Object lookup(ItemStack stack) {
        if (stack.isEmpty()) return null;
        Map<String, Integer> variant = aspects(stack);
        return variant == null || AspectApi.INSTANCE == null ? null : AspectApi.INSTANCE.create(variant);
    }

    @Nullable
    private static Map<String, Integer> aspects(ItemStack stack) {
        if (stack.getItem() instanceof CircleTalismanItem && CircleTalismanItem.storedPattern(stack) == 0) {
            return EMPTY_TALISMAN;
        }
        if (!(stack.getItem() instanceof BlockItem item)) return null;
        if (item.getBlock() instanceof WitchSaplingBlock) {
            return switch (WitchWoodType.fromStack(stack)) {
                case ROWAN -> null;
                case ALDER -> ALDER;
                case HAWTHORN -> HAWTHORN;
            };
        }
        if (item instanceof WickerBundleBlock.Item && WickerBundleBlock.isBloodied(stack)) return BLOODIED_WICKER;
        if (item instanceof BrambleBlock.Item && !BrambleBlock.isWild(stack)) return ENDER_BRAMBLE;
        if (item instanceof PlantMineBlock.Item) {
            return switch (PlantMineBlock.Variant.fromStack(stack)) {
                case ROSE_WEBS, DANDELION_WEBS, GRASS_WEBS -> null;
                case ROSE_INK, DANDELION_INK, GRASS_INK -> INK_MINE;
                case ROSE_THORNS, DANDELION_THORNS, GRASS_THORNS -> THORNS_MINE;
                case ROSE_SPROUTING, DANDELION_SPROUTING, GRASS_SPROUTING -> SPROUTING_MINE;
            };
        }
        return null;
    }

    private record AspectApi(Method resolveRegistries, Constructor<?> instance, Method ofEntries) {
        private static final String PACKAGE = "com.leclowndu93150.thaumaturge.";
        private static final AspectApi INSTANCE = bind();
        private static boolean reportedError;

        private static AspectApi bind() {
            try {
                return new AspectApi(
                        Class.forName(PACKAGE + "serialization.RegistryContext").getMethod("resolve", HolderLookup.Provider.class),
                        Class.forName(PACKAGE + "api.aspect.AspectInstance").getConstructor(Holder.class, int.class),
                        Class.forName(PACKAGE + "api.aspect.AspectList").getMethod("ofEntries", List.class));
            } catch (ReflectiveOperationException | LinkageError exception) {
                Witchery.LOGGER.warn("Could not bind Thaumaturge's item aspect API", exception);
                return null;
            }
        }

        @Nullable
        private Object create(Map<String, Integer> variant) {
            try {
                var registries = (HolderLookup.Provider) resolveRegistries.invoke(null, (Object) null);
                var aspects = registries.lookup(ThaumaturgeCompat.ASPECTS).orElse(null);
                if (aspects == null) return null;
                var entries = new ArrayList<>(variant.size());
                for (var entry : variant.entrySet()) {
                    var key = ResourceKey.create(ThaumaturgeCompat.ASPECTS, ResourceLocation.fromNamespaceAndPath("thaumaturge", entry.getKey()));
                    var aspect = aspects.get(key).orElse(null);
                    if (aspect == null) return null;
                    entries.add(instance.newInstance(aspect, entry.getValue()));
                }
                return ofEntries.invoke(null, entries);
            } catch (ReflectiveOperationException exception) {
                if (!reportedError) {
                    reportedError = true;
                    Witchery.LOGGER.warn("Could not resolve Witchery's Thaumaturge item aspects", exception);
                }
                return null;
            }
        }
    }
}
