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

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class WitcheryVillagePools {
    public static final ResourceLocation BOOKSHOP = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/bookshop");
    public static final ResourceLocation APOTHECARY = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "village/apothecary");
    private static final List<String> VILLAGE_STYLES = List.of("plains", "desert", "savanna", "snowy", "taiga");

    private WitcheryVillagePools() {}

    @SubscribeEvent
    public static void addVillagePieces(ServerAboutToStartEvent event) {
        var registries = event.getServer().registryAccess();
        var pools = registries.registryOrThrow(Registries.TEMPLATE_POOL);
        var processors = registries.registryOrThrow(Registries.PROCESSOR_LIST);
        if (WitcheryServerConfig.GENERATE_BOOK_SHOPS.get()) addVillagePiece(pools, processors, BOOKSHOP, 15);
        if (WitcheryServerConfig.GENERATE_APOTHECARIES.get()) addVillagePiece(pools, processors, APOTHECARY, 15);
    }

    private static void addVillagePiece(Registry<StructureTemplatePool> pools, Registry<StructureProcessorList> processors,
            ResourceLocation template, int weight) {
        var processorList = processors.getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, template));
        StructurePoolElement element = StructurePoolElement.single(template.toString(), processorList)
                .apply(StructureTemplatePool.Projection.RIGID);
        for (String style : VILLAGE_STYLES) {
            var poolId = ResourceLocation.withDefaultNamespace("village/" + style + "/houses");
            pools.getOptional(poolId).ifPresent(pool -> addPiece(pool, template, element, weight));
        }
    }

    private static void addPiece(StructureTemplatePool pool, ResourceLocation template, StructurePoolElement element, int weight) {
        var access = (StructureTemplatePoolAccessor) pool;
        var entries = new ArrayList<>(access.witchery$getRawTemplates());
        if (entries.stream().anyMatch(entry -> limitedBuilding(entry.getFirst()).filter(template::equals).isPresent())) return;
        entries.add(Pair.of(element, weight));
        access.witchery$setRawTemplates(entries);
        access.witchery$getTemplates().addAll(Collections.nCopies(weight, element));
    }

    public static boolean isBookshop(StructurePoolElement element) {
        return limitedBuilding(element).filter(BOOKSHOP::equals).isPresent();
    }

    public static Optional<ResourceLocation> limitedBuilding(StructurePoolElement element) {
        return element instanceof SinglePoolElement single
                ? ((SinglePoolElementAccessor) single).witchery$getTemplate().left()
                        .filter(id -> id.equals(BOOKSHOP) || id.equals(APOTHECARY))
                : Optional.empty();
    }
}
