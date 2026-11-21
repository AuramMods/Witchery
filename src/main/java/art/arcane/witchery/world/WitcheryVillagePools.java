package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.mixin.SinglePoolElementAccessor;
import art.arcane.witchery.mixin.StructureTemplatePoolAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
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

    private WitcheryVillagePools() {}

    @SubscribeEvent
    public static void addVillagePieces(ServerAboutToStartEvent event) {
        var registries = event.getServer().registryAccess();
        var pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        var processors = registries.registryOrThrow(Registries.PROCESSOR_LIST);
        if (WitcheryServerConfig.GENERATE_BOOK_SHOPS.get()) addVillagePiece(pools, processors, BOOKSHOP, 15);
        if (WitcheryServerConfig.GENERATE_APOTHECARIES.get()) addVillagePiece(pools, processors, APOTHECARY, 15);
        if (WitcheryServerConfig.GENERATE_WITCH_HUTS.get()) addVillagePiece(pools, processors, WITCH_HUT, 10);
        if (WitcheryServerConfig.TOWN_WALL_MODE.get() > 0) {
            addVillagePiece(pools, processors, WALL_ANCHOR, WitcheryServerConfig.TOWN_WALL_WEIGHT.get());
        }
        if (WitcheryServerConfig.TOWN_KEEP_MODE.get() > 0) {
            addVillagePiece(pools, processors, KEEP, WitcheryServerConfig.TOWN_KEEP_WEIGHT.get(), 1, true);
        }
        addVillagePiece(pools, processors, WATCHTOWER, WitcheryServerConfig.TOWN_GUARD_TOWER_CLUSTER_WEIGHT.get(),
                WitcheryServerConfig.TOWN_GUARD_TOWER_CLUSTER_GROUPS.get(), true);
    }

    private static void addVillagePiece(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors,
            ResourceLocation template, int weight) {
        addVillagePiece(pools, processors, template, weight, 1, false);
    }

    private static void addVillagePiece(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors,
            ResourceLocation template, int weight, int groups, boolean usesDistrictPalette) {
        if (weight == 0 || groups == 0) return;
        for (String style : VILLAGE_STYLES) {
            var poolId = ResourceLocation.withDefaultNamespace("village/" + style + "/houses");
            var processorId = usesDistrictPalette && (style.equals("desert") || style.equals("snowy"))
                    ? template.withSuffix("_" + style) : template;
            var processorList = processors.getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, processorId));
            var element = StructurePoolElement.single(template.toString(), processorList);
            pools.getOptional(poolId).ifPresent(pool -> addPieces(pool, template, element, weight, groups));
        }
    }

    private static void addPieces(StructureTemplatePool pool, ResourceLocation template,
            Function<StructureTemplatePool.Projection, ? extends StructurePoolElement> createElement,
            int weight, int groups) {
        var access = (StructureTemplatePoolAccessor) pool;
        var entries = new ArrayList<>(access.witchery$getRawTemplates());
        if (entries.stream().anyMatch(entry -> limitedBuilding(entry.getFirst()).filter(template::equals).isPresent())) return;
        for (int group = 0; group < groups; group++) {
            var element = createElement.apply(StructureTemplatePool.Projection.RIGID);
            for (int remaining = weight; remaining > 0; remaining -= MAX_POOL_ENTRY_WEIGHT) {
                entries.add(Pair.of(element, Math.min(remaining, MAX_POOL_ENTRY_WEIGHT)));
            }
            access.witchery$getTemplates().addAll(Collections.nCopies(weight, element));
        }
        access.witchery$setRawTemplates(entries);
    }

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
