package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.mixin.SinglePoolElementAccessor;
import art.arcane.witchery.mixin.StructureTemplatePoolAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class WitcheryVillagePools {
    public static final ResourceLocation BOOKSHOP = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/bookshop");
    public static final ResourceLocation APOTHECARY = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/apothecary");
    public static final ResourceLocation WITCH_HUT = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/witch_hut");
    public static final ResourceLocation KEEP = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/keep");
    public static final ResourceLocation WALL_ANCHOR = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/wall_anchor");
    public static final ResourceLocation WATCHTOWER = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/watchtower");
    private static final int MAX_POOL_ENTRY_WEIGHT = 150;
    private static final List<String> VILLAGE_STYLES = List.of("plains", "desert", "savanna", "snowy", "taiga");
    private static final List<String> TREK_ADDITIONAL_STYLES = List.of("swamp_vanilla", "cherry");

    private WitcheryVillagePools() {}

    @SubscribeEvent
    public static void addVillagePieces(ServerAboutToStartEvent event) {
        var registries = event.getServer().registryAccess();
        var pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        var processors = registries.registryOrThrow(Registries.PROCESSOR_LIST);
        var vanilla = housePools(pools, ResourceLocation.DEFAULT_NAMESPACE, VILLAGE_STYLES);
        var houses = new ArrayList<>(vanilla);
        houses.addAll(housePools(pools, "trek", VILLAGE_STYLES));
        houses.addAll(housePools(pools, "trek", TREK_ADDITIONAL_STYLES));
        if (WitcheryServerConfig.GENERATE_BOOK_SHOPS.get()) addVillagePiece(houses, processors, BOOKSHOP, 15);
        if (WitcheryServerConfig.GENERATE_APOTHECARIES.get()) addVillagePiece(houses, processors, APOTHECARY, 15);
        if (WitcheryServerConfig.GENERATE_WITCH_HUTS.get()) addVillagePiece(houses, processors, WITCH_HUT, 10);
        if (WitcheryServerConfig.TOWN_WALL_MODE.get() > 0) {
            addVillagePiece(houses, processors, WALL_ANCHOR, WitcheryServerConfig.TOWN_WALL_WEIGHT.get());
        }
        if (WitcheryServerConfig.TOWN_KEEP_MODE.get() > 0) {
            addVillagePiece(houses, processors, KEEP, WitcheryServerConfig.TOWN_KEEP_WEIGHT.get(), 1, true);
        }
        addVillagePiece(houses, processors, WATCHTOWER, WitcheryServerConfig.TOWN_GUARD_TOWER_CLUSTER_WEIGHT.get(),
                WitcheryServerConfig.TOWN_GUARD_TOWER_CLUSTER_GROUPS.get(), true);
        for (var village : vanilla) addVanillaClusters(village.pool(), village.style());
    }

    private static List<VillageHouses> housePools(Registry<StructureTemplatePool> pools, String namespace, List<String> styles) {
        var houses = new ArrayList<VillageHouses>();
        for (String style : styles) {
            var poolId = ResourceLocation.fromNamespaceAndPath(namespace, "village/" + style + "/houses");
            pools.getOptional(poolId).ifPresent(pool -> houses.add(new VillageHouses(style, pool)));
        }
        return houses;
    }

    private static void addVillagePiece(List<VillageHouses> houses, Registry<StructureProcessorList> processors,
            ResourceLocation template, int weight) {
        addVillagePiece(houses, processors, template, weight, 1, false);
    }

    private static void addVillagePiece(List<VillageHouses> houses, Registry<StructureProcessorList> processors,
            ResourceLocation template, int weight, int groups, boolean usesDistrictPalette) {
        if (weight == 0 || groups == 0) return;
        for (var village : houses) {
            String style = village.style();
            var processorId = usesDistrictPalette && (style.equals("desert") || style.equals("snowy"))
                    ? template.withSuffix("_" + style) : template;
            var processorList = processors.getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, processorId));
            var element = StructurePoolElement.single(template.toString(), processorList);
            addPieces(village.pool(), template, element, weight, groups);
        }
    }

    private record VillageHouses(String style, StructureTemplatePool pool) {}

    private static void addPieces(StructureTemplatePool pool, ResourceLocation template,
            Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> createElement,
            int weight, int groups) {
        var access = (StructureTemplatePoolAccessor) pool;
        var entries = new ArrayList<>(access.witchery$getRawTemplates());
        if (entries.stream().anyMatch(entry -> limitedBuilding(entry.getFirst()).filter(template::equals).isPresent())) return;
        for (int group = 0; group < groups; group++) {
            var element = createElement.apply(StructureTemplatePool.Projection.RIGID);
            appendWeighted(access, entries, element, weight);
        }
        access.witchery$setRawTemplates(entries);
    }

    private static void addVanillaClusters(StructureTemplatePool pool, String style) {
        var access = (StructureTemplatePoolAccessor) pool;
        var entries = new ArrayList<>(access.witchery$getRawTemplates());
        if (entries.stream().anyMatch(entry -> clusterLimit(entry.getFirst()) != null)) return;

        var families = new EnumMap<VillageBuildingFamily, List<SinglePoolElement>>(VillageBuildingFamily.class);
        for (var entry : entries) {
            if (!(entry.getFirst() instanceof SinglePoolElement original)) continue;
            var family = templateId(original).map(id -> VillageBuildingFamily.forTemplate(style, id)).orElse(null);
            if (family != null) families.computeIfAbsent(family, key -> new ArrayList<>()).add(original);
        }
        for (var entry : families.entrySet()) {
            var settings = WitcheryServerConfig.villageCluster(entry.getKey());
            int groups = settings.groups().get();
            int weight = settings.weight().get();
            if (groups == 0 || weight == 0) continue;
            var quota = new ClusterLimit(settings.min().get(), settings.max().get());
            var variants = entry.getValue();
            for (int group = 0; group < groups; group++) {
                var original = variants.get(group % variants.size());
                var template = templateId(original).orElseThrow().toString();
                var processors = ((SinglePoolElementAccessor) original).witchery$getProcessors();
                var create = original instanceof LegacySinglePoolElement ? StructurePoolElement.legacy(template, processors)
                        : StructurePoolElement.single(template, processors);
                var extra = create.apply(original.getProjection());
                ((ClusteredElement) extra).witchery$setClusterLimit(quota);
                appendWeighted(access, entries, extra, weight);
            }
        }
        access.witchery$setRawTemplates(entries);
    }

    private static void appendWeighted(StructureTemplatePoolAccessor access, List<Pair<StructurePoolElement, Integer>> entries,
            StructurePoolElement element, int weight) {
        for (int remaining = weight; remaining > 0; remaining -= MAX_POOL_ENTRY_WEIGHT) {
            entries.add(Pair.of(element, Math.min(remaining, MAX_POOL_ENTRY_WEIGHT)));
        }
        access.witchery$getTemplates().addAll(Collections.nCopies(weight, element));
    }

    @Nullable
    public static ClusterLimit clusterLimit(StructurePoolElement element) {
        return element instanceof ClusteredElement grouped ? grouped.witchery$getClusterLimit() : null;
    }

    public interface ClusteredElement {
        @Nullable ClusterLimit witchery$getClusterLimit();
        void witchery$setClusterLimit(ClusterLimit limit);
    }

    public record ClusterLimit(int min, int max) {}

    public static boolean isBookshop(StructurePoolElement element) {
        return limitedBuilding(element).filter(BOOKSHOP::equals).isPresent();
    }

    public static Optional<ResourceLocation> limitedBuilding(StructurePoolElement element) {
        return templateId(element).filter(id -> id.equals(BOOKSHOP) || id.equals(APOTHECARY) || id.equals(WITCH_HUT)
                || id.equals(KEEP) || id.equals(WATCHTOWER) || id.equals(WALL_ANCHOR));
    }

    public static Optional<ResourceLocation> templateId(StructurePoolElement element) {
        return element instanceof SinglePoolElement single
                ? ((SinglePoolElementAccessor) single).witchery$getTemplate().left()
                : Optional.empty();
    }
}
